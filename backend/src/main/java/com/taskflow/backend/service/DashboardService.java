package com.taskflow.backend.service;

import com.taskflow.backend.dto.DashboardDto;
import com.taskflow.backend.dto.OverdueTaskDto;
import com.taskflow.backend.model.Task;
import com.taskflow.backend.model.User;
import com.taskflow.backend.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final TaskRepository taskRepo;
    private final WorkspaceService workspaceService;

    public DashboardService(TaskRepository taskRepo, WorkspaceService workspaceService) {
        this.taskRepo = taskRepo;
        this.workspaceService = workspaceService;
    }

    public DashboardDto dashboard(Long workspaceId, User actor) {
        workspaceService.requireManager(workspaceId, actor);

        List<Task> tasks = taskRepo.findByProjectWorkspaceId(workspaceId);
        LocalDate today = LocalDate.now();

        Map<String, Integer> byStatus = new LinkedHashMap<>();
        byStatus.put("TODO", 0);
        byStatus.put("IN_PROGRESS", 0);
        byStatus.put("COMPLETED", 0);

        Map<String, Integer> perPerson = new LinkedHashMap<>();
        List<OverdueTaskDto> overdue = new ArrayList<>();

        for (Task t : tasks) {
            byStatus.merge(t.getStatus(), 1, Integer::sum);
            boolean open = !"COMPLETED".equals(t.getStatus());

            if (open && t.getAssignee() != null) {
                perPerson.merge(t.getAssignee().getName(), 1, Integer::sum);
            }
            if (open && t.getDueDate() != null && t.getDueDate().isBefore(today)) {
                String who = t.getAssignee() == null ? "Unassigned" : t.getAssignee().getName();
                overdue.add(new OverdueTaskDto(t.getId(), t.getTitle(), who, t.getDueDate()));
            }
        }
        return new DashboardDto(tasks.size(), byStatus, overdue.size(), overdue, perPerson);
    }
}