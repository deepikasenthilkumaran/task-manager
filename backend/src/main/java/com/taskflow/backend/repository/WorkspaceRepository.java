package com.taskflow.backend.repository;

import com.taskflow.backend.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
}