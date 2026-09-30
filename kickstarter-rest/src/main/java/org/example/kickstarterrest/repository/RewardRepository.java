package org.example.kickstarterrest.repository;

import org.example.kickstarterrest.entity.RewardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface RewardRepository extends JpaRepository<RewardEntity, Long> {
    List<RewardEntity> findByProjectId(Long projectId);
    void deleteByProjectId(Long projectId);

    @Query("SELECT r FROM RewardEntity r WHERE " +
            "(:projectId IS NULL OR r.project.id = :projectId) AND " +
            "(:title IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:minPrice IS NULL OR r.minPrice >= :minPrice)")
    Page<RewardEntity> searchRewards(@Param("projectId") Long projectId,
                                     @Param("title") String title,
                                     @Param("minPrice") BigDecimal minPrice,
                                     Pageable pageable);
}