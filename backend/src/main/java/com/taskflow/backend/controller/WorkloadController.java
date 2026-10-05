package com.taskflow.backend.controller;

import com.taskflow.backend.dto.WorkloadDto;
import com.taskflow.backend.service.WorkloadService;
import com.taskflow.backend.service.WorkspaceService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces/{id}")
public class WorkloadController {

    private final WorkloadService workloadService;
    private final WorkspaceService workspaceService;

    public WorkloadController(WorkloadService workloadService, WorkspaceService workspaceService) {
        this.workloadService = workloadService;
        this.workspaceService = workspaceService;
    }

    @GetMapping("/workload")
    public List<WorkloadDto> workload(@PathVariable Long id, Authentication auth) {
        return workloadService.workload(id, workspaceService.currentUser(auth.getName()));
    }

    @GetMapping("/suggest-assignee")
    public WorkloadDto suggest(@PathVariable Long id, Authentication auth) {
        return workloadService.suggestAssignee(id, workspaceService.currentUser(auth.getName()));
    }
}