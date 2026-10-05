package com.taskflow.backend.controller;

import com.taskflow.backend.dto.DashboardDto;
import com.taskflow.backend.service.DashboardService;
import com.taskflow.backend.service.WorkspaceService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class DashboardController {

    private final DashboardService dashboardService;
    private final WorkspaceService workspaceService;

    public DashboardController(DashboardService dashboardService, WorkspaceService workspaceService) {
        this.dashboardService = dashboardService;
        this.workspaceService = workspaceService;
    }

    @GetMapping("/api/workspaces/{id}/dashboard")
    public DashboardDto dashboard(@PathVariable Long id, Authentication auth) {
        return dashboardService.dashboard(id, workspaceService.currentUser(auth.getName()));
    }
}