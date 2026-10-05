package com.taskflow.backend.dto;

import java.time.LocalDate;

public record OverdueTaskDto(Long id, String title, String assignee, LocalDate dueDate) {}