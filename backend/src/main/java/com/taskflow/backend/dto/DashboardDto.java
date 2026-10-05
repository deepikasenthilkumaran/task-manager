package com.taskflow.backend.dto;

import java.util.List;
import java.util.Map;

public record DashboardDto(int totalTasks,
                           Map<String, Integer> byStatus,
                           int overdueCount,
                           List<OverdueTaskDto> overdueTasks,
                           Map<String, Integer> openTasksPerPerson) {}