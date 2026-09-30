package org.example.kickstarterrest.repository;

import org.example.kickstarterrest.entity.ProjectEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
    boolean existsByTitle(String title);

    @Override
    @EntityGraph(attributePaths = "author")
    Optional<ProjectEntity> findById(Long id);
}