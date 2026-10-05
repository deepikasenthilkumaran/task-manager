package com.taskflow.backend.dto;

import java.time.LocalDate;

public record UpdateTaskRequest(String title, String description, String priority,
                                LocalDate dueDate, String labels) {}