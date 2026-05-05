package org.example.kickstarterrest.service;

import org.example.kickstarterapicontract.dto.PledgeRequest;
import org.example.kickstarterapicontract.dto.PledgeResponse;
import org.example.kickstarterapicontract.dto.RewardResponse;
import org.example.kickstarterrest.exception.ResourceNotFoundException;
import org.example.kickstarterrest.storage.InMemoryStorage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class PledgeService {
    private final InMemoryStorage storage;
    private final ProjectService projectService;
    private final RewardService rewardService;

    public PledgeService(InMemoryStorage storage, @Lazy ProjectService projectService, @Lazy RewardService rewardService) {
        this.storage = storage;
        this.projectService = projectService;
        this.rewardService = rewardService;
    }

    public PledgeResponse findById(Long id) {
        if (!storage.pledges.containsKey(id)) throw new ResourceNotFoundException("Взнос", id);
        return storage.pledges.get(id);
    }

    public List<PledgeResponse> findAll() {
        return storage.pledges.values().stream()
                .sorted(Comparator.comparingLong(PledgeResponse::getPledgeId)).toList();
    }

    public PledgeResponse create(PledgeRequest request) {
        projectService.findById(request.projectId());
        RewardResponse reward = rewardService.findRewardById(request.rewardId());

        if (request.pledge().compareTo(reward.getMinPrice()) < 0) {
            throw new IllegalArgumentException("Сумма взноса меньше минимальной цены вознаграждения!");
        }

        projectService.addPledgedAmount(request.projectId(), request.pledge());

        long id = storage.pledgeSequence.incrementAndGet();
        PledgeResponse pledge = PledgeResponse.builder()
                .pledgeId(id).status("SUCCESSFUL").transactionDate(OffsetDateTime.now()).build();
        storage.pledges.put(id, pledge);
        return pledge;
    }
}