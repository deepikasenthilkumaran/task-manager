package com.taskflow.backend.controller;

import com.taskflow.backend.dto.LoginRequest;
import com.taskflow.backend.dto.RegisterRequest;
import com.taskflow.backend.model.User;
import com.taskflow.backend.repository.UserRepository;
import com.taskflow.backend.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            return ResponseEntity.badRequest().body("Email already registered");
        }
        User user = new User();
        user.setName(req.name());
        user.setEmail(req.email());
        user.setPassword(passwordEncoder.encode(req.password()));
        userRepository.save(user);
        return ResponseEntity.ok("Registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        User user = userRepository.findByEmail(req.email()).orElse(null);
        if (user == null || !passwordEncoder.matches(req.password(), user.getPassword())) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return ResponseEntity.ok(Map.of(
                "token", token,
                "name", user.getName(),
                "role", user.getRole()));
    }
}