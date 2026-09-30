package org.example.kickstarterrest.service;

import lombok.RequiredArgsConstructor;
import org.example.kickstarterapicontract.dto.PagedResponse;
import org.example.kickstarterapicontract.dto.PledgeRequest;
import org.example.kickstarterapicontract.dto.PledgeResponse;
import org.example.kickstarterapicontract.dto.PledgeStatus;
import org.example.kickstarterapicontract.exception.ResourceNotFoundException;
import org.example.kickstarterrest.entity.PledgeEntity;
import org.example.kickstarterrest.entity.ProjectEntity;
import org.example.kickstarterrest.entity.RewardEntity;
import org.example.kickstarterrest.entity.UserEntity;
import org.example.kickstarterrest.event.PledgeEventPublisher;
import org.example.kickstarterrest.repository.PledgeRepository;
import org.example.kickstarterrest.repository.ProjectRepository;
import org.example.kickstarterrest.repository.RewardRepository;
import org.example.kickstarterrest.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class PledgeService {

    private final PledgeRepository pledgeRepository;
    private final ProjectRepository projectRepository;
    private final RewardRepository rewardRepository;
    private final UserRepository userRepository;
    private final PledgeEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public PledgeResponse findById(Long id) {
        return pledgeRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Взнос", id));
    }

    @Transactional(readOnly = true)
    public PagedResponse<PledgeResponse> findAllPledges(Long projectId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<PledgeEntity> entityPage = (projectId != null)
                ? pledgeRepository.findByProjectId(projectId, pageRequest)
                : pledgeRepository.findAll(pageRequest);

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
    public PledgeResponse create(PledgeRequest request) {
        UserEntity user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", request.userId()));
        ProjectEntity project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Проект", request.projectId()));
        RewardEntity reward = rewardRepository.findById(request.rewardId())
                .orElseThrow(() -> new ResourceNotFoundException("Reward", request.rewardId()));

        if (request.pledge().compareTo(reward.getMinPrice()) < 0) {
            throw new IllegalArgumentException("Сумма взноса меньше минимальной цены вознаграждения!");
        }

        PledgeEntity entity = PledgeEntity.builder()
                .project(project)
                .reward(reward)
                .user(user)
                .amount(request.pledge())
                .status(PledgeStatus.AUTHORIZED)
                .transactionDate(OffsetDateTime.now())
                .build();

        PledgeEntity saved = pledgeRepository.save(entity);

        project.setPledged(project.getPledged().add(request.pledge()));
        projectRepository.save(project);

        PledgeResponse response = toResponse(saved);
        eventPublisher.publishCreated(response);
        return response;
    }

    public PledgeResponse toResponse(PledgeEntity entity) {
        return PledgeResponse.builder()
                .pledgeId(entity.getId())
                .projectId(entity.getProject().getId())
                .rewardId(entity.getReward().getId())
                .userId(entity.getUser().getId())
                .amount(entity.getAmount())
                .status(entity.getStatus())
                .transactionDate(entity.getTransactionDate())
                .build();
    }
}