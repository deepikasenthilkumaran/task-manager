package com.taskflow.backend.service;

import com.taskflow.backend.dto.CreateProjectRequest;
import com.taskflow.backend.model.*;
import com.taskflow.backend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class WorkspaceService {

    private final UserRepository userRepo;
    private final WorkspaceRepository workspaceRepo;
    private final WorkspaceMemberRepository memberRepo;
    private final ProjectRepository projectRepo;

    public WorkspaceService(UserRepository userRepo, WorkspaceRepository workspaceRepo,
                            WorkspaceMemberRepository memberRepo, ProjectRepository projectRepo) {
        this.userRepo = userRepo;
        this.workspaceRepo = workspaceRepo;
        this.memberRepo = memberRepo;
        this.projectRepo = projectRepo;
    }

    public User currentUser(String email) {
        return userRepo.findByEmail(email).orElseThrow();
    }

    @Transactional
    public Workspace create(String name, User owner) {
        Workspace w = new Workspace();
        w.setName(name);
        w.setOwner(owner);
        workspaceRepo.save(w);

        WorkspaceMember m = new WorkspaceMember();
        m.setWorkspace(w);
        m.setUser(owner);
        m.setRole("MANAGER");
        memberRepo.save(m);
        return w;
    }

    public List<Workspace> listFor(User user) {
        return memberRepo.findByUserId(user.getId()).stream()
                .map(WorkspaceMember::getWorkspace).toList();
    }

    public WorkspaceMember requireMember(Long workspaceId, User user) {
        return memberRepo.findByWorkspaceIdAndUserId(workspaceId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "You are not a member of this workspace"));
    }

    public void requireManager(Long workspaceId, User user) {
        if (!"MANAGER".equals(requireMember(workspaceId, user).getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Managers only");
        }
    }

    public Project createProject(Long workspaceId, CreateProjectRequest req, User actor) {
        requireManager(workspaceId, actor);
        Project p = new Project();
        p.setName(req.name());
        p.setDescription(req.description());
        p.setWorkspace(workspaceRepo.findById(workspaceId).orElseThrow());
        return projectRepo.save(p);
    }

    public List<Project> listProjects(Long workspaceId, User actor) {
        requireMember(workspaceId, actor);
        return projectRepo.findByWorkspaceId(workspaceId);
    }
}