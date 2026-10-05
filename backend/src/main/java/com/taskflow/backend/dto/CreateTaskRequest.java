package com.taskflow.backend.dto;

import java.time.LocalDate;

public record CreateTaskRequest(String title, String description, String priority,
                                LocalDate dueDate, String labels) {}