package com.taskflow.backend.service;

import com.taskflow.backend.dto.CreateTaskRequest;
import com.taskflow.backend.dto.UpdateTaskRequest;
import com.taskflow.backend.model.Project;
import com.taskflow.backend.model.Task;
import com.taskflow.backend.model.User;
import com.taskflow.backend.repository.CommentRepository;
import com.taskflow.backend.repository.ProjectRepository;
import com.taskflow.backend.repository.TaskRepository;
import com.taskflow.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
public class TaskService {

    private static final Set<String> STATUSES = Set.of("TODO", "IN_PROGRESS", "COMPLETED");
    private static final Set<String> PRIORITIES = Set.of("LOW", "MEDIUM", "HIGH");

    private final TaskRepository taskRepo;
    private final ProjectRepository projectRepo;
    private final UserRepository userRepo;
    private final CommentRepository commentRepo;
    private final WorkspaceService workspaceService;
    private final WorkloadService workloadService;

    public TaskService(TaskRepository taskRepo, ProjectRepository projectRepo,
                       UserRepository userRepo, CommentRepository commentRepo,
                       WorkspaceService workspaceService, WorkloadService workloadService) {
        this.taskRepo = taskRepo;
        this.projectRepo = projectRepo;
        this.userRepo = userRepo;
        this.commentRepo = commentRepo;
        this.workspaceService = workspaceService;
        this.workloadService = workloadService;
    }

    private Task load(Long id) {
        return taskRepo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    private Long workspaceOf(Task t) {
        return t.getProject().getWorkspace().getId();
    }

    public Task create(Long projectId, CreateTaskRequest req, User actor) {
        Project project = projectRepo.findById(projectId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        workspaceService.requireMember(project.getWorkspace().getId(), actor);

        Task t = new Task();
        t.setTitle(req.title());
        t.setDescription(req.description());
        if (req.priority() != null) {
            if (!PRIORITIES.contains(req.priority())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Priority must be LOW, MEDIUM or HIGH");
            }
            t.setPriority(req.priority());
        }
        t.setDueDate(req.dueDate());
        t.setLabels(req.labels());
        t.setProject(project);
        return taskRepo.save(t);
    }

    public List<Task> listByProject(Long projectId, User actor) {
        Project project = projectRepo.findById(projectId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        workspaceService.requireMember(project.getWorkspace().getId(), actor);
        return taskRepo.findByProjectId(projectId);
    }

    public Task update(Long taskId, UpdateTaskRequest req, User actor) {
        Task t = load(taskId);
        workspaceService.requireMember(workspaceOf(t), actor);

        if (req.title() != null) {
            if (req.title().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title cannot be empty");
            }
            t.setTitle(req.title());
        }
        if (req.description() != null) t.setDescription(req.description());
        if (req.priority() != null) {
            if (!PRIORITIES.contains(req.priority())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Priority must be LOW, MEDIUM or HIGH");
            }
            t.setPriority(req.priority());
        }
        if (req.dueDate() != null) t.setDueDate(req.dueDate());
        if (req.labels() != null) t.setLabels(req.labels());
        return taskRepo.save(t);
    }

    public Task changeStatus(Long taskId, String status, User actor) {
        Task t = load(taskId);
        workspaceService.requireMember(workspaceOf(t), actor);
        if (!STATUSES.contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be TODO, IN_PROGRESS or COMPLETED");
        }
        t.setStatus(status);
        return taskRepo.save(t);
    }

    public Task assign(Long taskId, Long assigneeId, boolean force, User actor) {
        Task t = load(taskId);
        Long workspaceId = workspaceOf(t);
        workspaceService.requireManager(workspaceId, actor);

        User assignee = userRepo.findById(assigneeId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        workspaceService.requireMember(workspaceId, assignee);

        if (!force && workloadService.isOverloaded(workspaceId, assigneeId, actor)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    assignee.getName() + " is overloaded. Resend with force=true to assign anyway.");
        }
        t.setAssignee(assignee);
        return taskRepo.save(t);
    }

    @Transactional
    public void delete(Long taskId, User actor) {
        Task t = load(taskId);
        workspaceService.requireManager(workspaceOf(t), actor);
        commentRepo.deleteAll(commentRepo.findByTaskIdOrderByCreatedAtAsc(taskId));
        taskRepo.delete(t);
    }
}