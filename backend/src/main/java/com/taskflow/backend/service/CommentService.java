package com.taskflow.backend.service;

import com.taskflow.backend.model.Comment;
import com.taskflow.backend.model.Task;
import com.taskflow.backend.model.User;
import com.taskflow.backend.repository.CommentRepository;
import com.taskflow.backend.repository.TaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepo;
    private final TaskRepository taskRepo;
    private final WorkspaceService workspaceService;

    public CommentService(CommentRepository commentRepo, TaskRepository taskRepo,
                          WorkspaceService workspaceService) {
        this.commentRepo = commentRepo;
        this.taskRepo = taskRepo;
        this.workspaceService = workspaceService;
    }

    private Task loadForMember(Long taskId, User actor) {
        Task task = taskRepo.findById(taskId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        workspaceService.requireMember(task.getProject().getWorkspace().getId(), actor);
        return task;
    }

    public Comment add(Long taskId, String text, User actor) {
        Task task = loadForMember(taskId, actor);
        if (text == null || text.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment cannot be empty");
        }
        Comment c = new Comment();
        c.setText(text.trim());
        c.setTask(task);
        c.setAuthor(actor);
        return commentRepo.save(c);
    }

    public List<Comment> list(Long taskId, User actor) {
        loadForMember(taskId, actor);
        return commentRepo.findByTaskIdOrderByCreatedAtAsc(taskId);
    }
}