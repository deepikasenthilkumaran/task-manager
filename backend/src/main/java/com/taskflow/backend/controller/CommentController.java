package com.taskflow.backend.controller;

import com.taskflow.backend.dto.CreateCommentRequest;
import com.taskflow.backend.model.Comment;
import com.taskflow.backend.service.CommentService;
import com.taskflow.backend.service.WorkspaceService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
public class CommentController {

    private final CommentService commentService;
    private final WorkspaceService workspaceService;

    public CommentController(CommentService commentService, WorkspaceService workspaceService) {
        this.commentService = commentService;
        this.workspaceService = workspaceService;
    }

    @PostMapping
    public Comment add(@PathVariable Long taskId, @RequestBody CreateCommentRequest req,
                       Authentication auth) {
        return commentService.add(taskId, req.text(), workspaceService.currentUser(auth.getName()));
    }

    @GetMapping
    public List<Comment> list(@PathVariable Long taskId, Authentication auth) {
        return commentService.list(taskId, workspaceService.currentUser(auth.getName()));
    }
}