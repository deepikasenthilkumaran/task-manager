package com.taskflow.backend.controller;

import com.taskflow.backend.model.Notification;
import com.taskflow.backend.service.NotificationService;
import com.taskflow.backend.service.WorkspaceService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final WorkspaceService workspaceService;

    public NotificationController(NotificationService notificationService,
                                  WorkspaceService workspaceService) {
        this.notificationService = notificationService;
        this.workspaceService = workspaceService;
    }

    @GetMapping
    public List<Notification> mine(Authentication auth) {
        return notificationService.mine(workspaceService.currentUser(auth.getName()));
    }

    @PutMapping("/{id}/seen")
    public Notification seen(@PathVariable Long id, Authentication auth) {
        return notificationService.markSeen(id, workspaceService.currentUser(auth.getName()));
    }
}