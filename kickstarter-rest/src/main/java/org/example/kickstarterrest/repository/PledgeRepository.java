package org.example.kickstarterrest.repository;

import org.example.kickstarterrest.entity.PledgeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PledgeRepository extends JpaRepository<PledgeEntity, Long> {
    Page<PledgeEntity> findByProjectId(Long projectId, Pageable pageable);
    List<PledgeEntity> findByUserId(Long userId);
}