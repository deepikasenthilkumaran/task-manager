package com.taskflow.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "tasks")
@Data
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    private String status = "TODO";        // TODO, IN_PROGRESS, COMPLETED

    private String priority = "MEDIUM";    // LOW, MEDIUM, HIGH

    private LocalDate dueDate;

    private String labels;                 // for example: bug,urgent

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @Version
    private Long version;
}