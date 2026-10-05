package com.taskflow.backend.dto;

public record WorkloadDto(Long userId, String name, int openTasks, int score, String level) {}