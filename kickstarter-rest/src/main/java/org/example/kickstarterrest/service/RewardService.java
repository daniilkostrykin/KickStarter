package org.example.kickstarterrest.service;

import lombok.RequiredArgsConstructor;
import org.example.kickstarterapicontract.dto.*;
import org.example.kickstarterapicontract.exception.ResourceNotFoundException;
import org.example.kickstarterrest.event.RewardEventPublisher;
import org.example.kickstarterrest.storage.InMemoryStorage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class RewardService {

    private final InMemoryStorage storage;
    private final ProjectService projectService;
    private final RewardEventPublisher eventPublisher;


    public RewardResponse findRewardById(Long id) {
        return Optional.ofNullable(storage.rewards.get(id))
                .orElseThrow(() -> new ResourceNotFoundException("Reward", id));
    }

    public PagedResponse<RewardResponse> findAllRewards(Long projectId, String titleSearch, BigDecimal minPrice, int page, int size) {
        Stream<RewardResponse> stream = storage.rewards.values().stream()
                .sorted((r1, r2) -> r1.getId().compareTo(r2.getId()));

        if (projectId != null) {
            stream = stream.filter(r -> projectId.equals(r.getProjectId()));
        }
        if (titleSearch != null && !titleSearch.isBlank()) {
            String q = titleSearch.toLowerCase();
            stream = stream.filter(r -> r.getTitle() != null && r.getTitle().toLowerCase().contains(q));
        }
        if (minPrice != null) {
            stream = stream.filter(r -> r.getMinPrice() != null && r.getMinPrice().compareTo(minPrice) >= 0);
        }

        List<RewardResponse> allRewards = stream.toList();
        int totalElements = allRewards.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        int from = page * size;
        int to = Math.min(from + size, totalElements);
        List<RewardResponse> content = (from >= totalElements) ? List.of() : allRewards.subList(from, to);

        return new PagedResponse<>(content, page, size, totalElements, totalPages, page >= totalPages - 1);
    }

    public RewardResponse createReward(RewardRequest request) {
        ProjectResponse project = projectService.findById(request.projectId());

        Long id = storage.rewardSequence.incrementAndGet();
        RewardResponse reward = RewardResponse.builder()
                .id(id)
                .projectId(project.getId())
                .title(request.title())
                .description(request.description())
                .minPrice(request.minPrice())
                .build();
        storage.rewards.put(id, reward);

        eventPublisher.publishCreated(reward);
        return reward;
    }

    public RewardResponse updateReward(Long id, RewardRequest request) {
        RewardResponse existing = findRewardById(id);

        RewardResponse updated = RewardResponse.builder()
                .id(id)
                .projectId(existing.getProjectId())
                .title(request.title())
                .description(request.description())
                .minPrice(request.minPrice())
                .build();
        storage.rewards.put(id, updated);
        return updated;
    }

    public RewardResponse patchReward(Long id, PatchRewardRequest request) {
        RewardResponse existing = findRewardById(id);

        RewardResponse updated = RewardResponse.builder()
                .id(id)
                .projectId(existing.getProjectId())
                .title(request.title() != null ? request.title() : existing.getTitle())
                .description(request.description() != null ? request.description() : existing.getDescription())
                .minPrice(request.minPrice() != null ? request.minPrice() : existing.getMinPrice())
                .build();
        storage.rewards.put(id, updated);
        return updated;
    }

    public void deleteReward(Long id) {
        findRewardById(id);
        storage.rewards.remove(id);
    }

    public void deleteRewardsByProjectId(Long projectId) {
        List<Long> toDelete = storage.rewards.values().stream()
                .filter(r -> r.getProjectId().equals(projectId))
                .map(RewardResponse::getId)
                .toList();
        toDelete.forEach(storage.rewards::remove);
    }

}