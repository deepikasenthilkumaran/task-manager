package com.taskflow.backend.service;

import com.taskflow.backend.dto.WorkloadDto;
import com.taskflow.backend.model.Task;
import com.taskflow.backend.model.User;
import com.taskflow.backend.model.WorkspaceMember;
import com.taskflow.backend.repository.TaskRepository;
import com.taskflow.backend.repository.WorkspaceMemberRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class WorkloadService {

    private static final int BALANCED_FROM = 5;
    private static final int OVERLOADED_FROM = 10;

    private final TaskRepository taskRepo;
    private final WorkspaceMemberRepository memberRepo;
    private final WorkspaceService workspaceService;

    public WorkloadService(TaskRepository taskRepo, WorkspaceMemberRepository memberRepo,
                           WorkspaceService workspaceService) {
        this.taskRepo = taskRepo;
        this.memberRepo = memberRepo;
        this.workspaceService = workspaceService;
    }

    private int taskPoints(Task t) {
        int points = switch (t.getPriority()) {
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            default -> 1;
        };
        if (t.getDueDate() != null) {
            LocalDate today = LocalDate.now();
            if (t.getDueDate().isBefore(today)) {
                points += 3;
            } else if (!t.getDueDate().isAfter(today.plusDays(2))) {
                points += 2;
            }
        }
        return points;
    }

    private String levelFor(int score) {
        if (score >= OVERLOADED_FROM) return "OVERLOADED";
        if (score >= BALANCED_FROM) return "BALANCED";
        return "FREE";
    }

    public List<WorkloadDto> workload(Long workspaceId, User actor) {
        workspaceService.requireMember(workspaceId, actor);

        List<Task> open = taskRepo.findByProjectWorkspaceIdAndStatusNot(workspaceId, "COMPLETED");
        Map<Long, Integer> scores = new HashMap<>();
        Map<Long, Integer> counts = new HashMap<>();
        for (Task t : open) {
            if (t.getAssignee() == null) continue;
            Long uid = t.getAssignee().getId();
            scores.merge(uid, taskPoints(t), Integer::sum);
            counts.merge(uid, 1, Integer::sum);
        }

        List<WorkloadDto> result = new ArrayList<>();
        for (WorkspaceMember m : memberRepo.findByWorkspaceId(workspaceId)) {
            Long uid = m.getUser().getId();
            int score = scores.getOrDefault(uid, 0);
            result.add(new WorkloadDto(uid, m.getUser().getName(),
                    counts.getOrDefault(uid, 0), score, levelFor(score)));
        }
        result.sort(Comparator.comparingInt(WorkloadDto::score));
        return result;
    }

    public WorkloadDto suggestAssignee(Long workspaceId, User actor) {
        return workload(workspaceId, actor).get(0);
    }

    public boolean isOverloaded(Long workspaceId, Long userId, User actor) {
        return workload(workspaceId, actor).stream()
                .anyMatch(w -> w.userId().equals(userId) && w.level().equals("OVERLOADED"));
    }
}