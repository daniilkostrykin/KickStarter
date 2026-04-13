package org.example.kickstarterrest.service;

import org.example.kickstarterapicontract.dto.PatchRewardRequest;
import org.example.kickstarterapicontract.dto.RewardRequest;
import org.example.kickstarterapicontract.dto.RewardResponse;
import org.example.kickstarterrest.exception.ResourceNotFoundException;
import org.example.kickstarterrest.storage.InMemoryStorage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class RewardService {
    private final InMemoryStorage storage;
    private final ProjectService projectService;

    public RewardService(InMemoryStorage storage, @Lazy ProjectService projectService) {
        this.storage = storage;
        this.projectService = projectService;
    }

    public RewardResponse findById(Long id) {
        if (!storage.rewards.containsKey(id)) throw new ResourceNotFoundException("Вознаграждение", id);
        return storage.rewards.get(id);
    }

    public List<RewardResponse> findAll() {
        return storage.rewards.values().stream()
                .sorted(Comparator.comparingLong(RewardResponse::getId)).toList();
    }

    public RewardResponse create(RewardRequest request) {
        projectService.findById(request.projectId()); // Проверяем, что проект существует
        long id = storage.rewardSequence.incrementAndGet();
        RewardResponse reward = RewardResponse.builder()
                .id(id).title(request.title()).description(request.description())
                .minPrice(request.minPrice()).projectId(request.projectId()).build();
        storage.rewards.put(id, reward);
        return reward;
    }

    public RewardResponse patch(Long id, PatchRewardRequest request) {
        RewardResponse existing = findById(id);
        RewardResponse updated = RewardResponse.builder()
                .id(existing.getId()).projectId(existing.getProjectId())
                .title(request.title() != null ? request.title() : existing.getTitle())
                .description(request.description() != null ? request.description() : existing.getDescription())
                .minPrice(request.minPrice() != null ? request.minPrice() : existing.getMinPrice()).build();
        storage.rewards.put(id, updated);
        return updated;
    }
}