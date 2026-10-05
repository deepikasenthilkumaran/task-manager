package com.taskflow.backend.controller;

import com.taskflow.backend.dto.CreateTaskRequest;
import com.taskflow.backend.dto.StatusRequest;
import com.taskflow.backend.model.Task;
import com.taskflow.backend.service.TaskService;
import com.taskflow.backend.service.WorkspaceService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TaskController {

    private final TaskService taskService;
    private final WorkspaceService workspaceService;

    public TaskController(TaskService taskService, WorkspaceService workspaceService) {
        this.taskService = taskService;
        this.workspaceService = workspaceService;
    }

    @PostMapping("/api/projects/{projectId}/tasks")
    public Task create(@PathVariable Long projectId, @RequestBody CreateTaskRequest req,
                       Authentication auth) {
        return taskService.create(projectId, req, workspaceService.currentUser(auth.getName()));
    }

    @GetMapping("/api/projects/{projectId}/tasks")
    public List<Task> list(@PathVariable Long projectId, Authentication auth) {
        return taskService.listByProject(projectId, workspaceService.currentUser(auth.getName()));
    }

    @PutMapping("/api/tasks/{id}/status")
    public Task status(@PathVariable Long id, @RequestBody StatusRequest req, Authentication auth) {
        return taskService.changeStatus(id, req.status(), workspaceService.currentUser(auth.getName()));
    }

    @PutMapping("/api/tasks/{id}/assign")
    public Task assign(@PathVariable Long id, @RequestParam Long assigneeId,
                       @RequestParam(defaultValue = "false") boolean force, Authentication auth) {
        return taskService.assign(id, assigneeId, force, workspaceService.currentUser(auth.getName()));
    }
}