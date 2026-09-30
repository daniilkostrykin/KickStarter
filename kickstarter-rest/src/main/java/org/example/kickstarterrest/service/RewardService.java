package org.example.kickstarterrest.service;

import lombok.RequiredArgsConstructor;
import org.example.kickstarterapicontract.dto.PagedResponse;
import org.example.kickstarterapicontract.dto.PatchRewardRequest;
import org.example.kickstarterapicontract.dto.RewardRequest;
import org.example.kickstarterapicontract.dto.RewardResponse;
import org.example.kickstarterapicontract.exception.ResourceNotFoundException;
import org.example.kickstarterrest.entity.ProjectEntity;
import org.example.kickstarterrest.entity.RewardEntity;
import org.example.kickstarterrest.event.RewardEventPublisher;
import org.example.kickstarterrest.repository.ProjectRepository;
import org.example.kickstarterrest.repository.RewardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RewardService {

    private final RewardRepository rewardRepository;
    private final ProjectRepository projectRepository;
    private final RewardEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public RewardResponse findRewardById(Long id) {
        return rewardRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Reward", id));
    }

    @Transactional(readOnly = true)
    public PagedResponse<RewardResponse> findAllRewards(Long projectId, String titleSearch, BigDecimal minPrice, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<RewardEntity> entityPage = rewardRepository.searchRewards(projectId, titleSearch, minPrice, pageRequest);

        return new PagedResponse<>(
                entityPage.getContent().stream().map(this::toResponse).toList(),
                entityPage.getNumber(),
                entityPage.getSize(),
                entityPage.getTotalElements(),
                entityPage.getTotalPages(),
                entityPage.isLast()
        );
    }

    @Transactional
    public RewardResponse createReward(RewardRequest request) {
        ProjectEntity project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", request.projectId()));

        RewardEntity entity = RewardEntity.builder()
                .project(project)
                .title(request.title())
                .description(request.description())
                .minPrice(request.minPrice())
                .build();

        RewardEntity saved = rewardRepository.save(entity);
        RewardResponse response = toResponse(saved);
        eventPublisher.publishCreated(response);
        return response;
    }

    @Transactional
    public RewardResponse updateReward(Long id, RewardRequest request) {
        RewardEntity entity = rewardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reward", id));

        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setMinPrice(request.minPrice());

        return toResponse(rewardRepository.save(entity));
    }

    @Transactional
    public RewardResponse patchReward(Long id, PatchRewardRequest request) {
        RewardEntity entity = rewardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reward", id));

        if (request.title() != null) entity.setTitle(request.title());
        if (request.description() != null) entity.setDescription(request.description());
        if (request.minPrice() != null) entity.setMinPrice(request.minPrice());

        return toResponse(rewardRepository.save(entity));
    }

    @Transactional
    public void deleteReward(Long id) {
        RewardEntity entity = rewardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reward", id));
        rewardRepository.delete(entity);
    }

    @Transactional
    public void deleteRewardsByProjectId(Long projectId) {
        rewardRepository.deleteByProjectId(projectId);
    }

    public RewardResponse toResponse(RewardEntity entity) {
        return RewardResponse.builder()
                .id(entity.getId())
                .projectId(entity.getProject().getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .minPrice(entity.getMinPrice())
                .build();
    }
}