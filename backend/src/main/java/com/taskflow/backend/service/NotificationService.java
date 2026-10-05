package com.taskflow.backend.service;

import com.taskflow.backend.model.Notification;
import com.taskflow.backend.model.User;
import com.taskflow.backend.repository.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepo;

    public NotificationService(NotificationRepository notificationRepo) {
        this.notificationRepo = notificationRepo;
    }

    public void notify(User recipient, String message) {
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setMessage(message);
        notificationRepo.save(n);
    }

    public List<Notification> mine(User user) {
        return notificationRepo.findByRecipientIdOrderByCreatedAtDesc(user.getId());
    }

    public Notification markSeen(Long id, User user) {
        Notification n = notificationRepo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        if (!n.getRecipient().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your notification");
        }
        n.setSeen(true);
        return notificationRepo.save(n);
    }
}