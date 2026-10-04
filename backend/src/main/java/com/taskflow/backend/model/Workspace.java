package com.taskflow.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "workspaces")
@Data
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
}