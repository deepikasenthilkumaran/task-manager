package com.taskflow.backend.controller;

import com.taskflow.backend.dto.AddMemberRequest;
import com.taskflow.backend.dto.CreateProjectRequest;
import com.taskflow.backend.dto.CreateWorkspaceRequest;
import com.taskflow.backend.dto.MemberDto;
import com.taskflow.backend.model.Project;
import com.taskflow.backend.model.Workspace;
import com.taskflow.backend.model.WorkspaceMember;
import com.taskflow.backend.service.WorkspaceService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

    private final WorkspaceService service;

    public WorkspaceController(WorkspaceService service) {
        this.service = service;
    }

    @PostMapping
    public Workspace create(@RequestBody CreateWorkspaceRequest req, Authentication auth) {
        return service.create(req.name(), service.currentUser(auth.getName()));
    }

    @GetMapping
    public List<Workspace> mine(Authentication auth) {
        return service.listFor(service.currentUser(auth.getName()));
    }

    @GetMapping("/{id}/members")
    public List<MemberDto> members(@PathVariable Long id, Authentication auth) {
        return service.listMembers(id, service.currentUser(auth.getName()));
    }

    @PostMapping("/{id}/members")
    public WorkspaceMember addMember(@PathVariable Long id, @RequestBody AddMemberRequest req,
                                     Authentication auth) {
        return service.addMember(id, req.email(), req.role(), service.currentUser(auth.getName()));
    }

    @PostMapping("/{id}/projects")
    public Project createProject(@PathVariable Long id, @RequestBody CreateProjectRequest req,
                                 Authentication auth) {
        return service.createProject(id, req, service.currentUser(auth.getName()));
    }

    @GetMapping("/{id}/projects")
    public List<Project> projects(@PathVariable Long id, Authentication auth) {
        return service.listProjects(id, service.currentUser(auth.getName()));
    }
}