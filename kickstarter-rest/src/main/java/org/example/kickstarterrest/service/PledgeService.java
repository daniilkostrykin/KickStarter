package org.example.kickstarterrest.service;

import lombok.RequiredArgsConstructor;
import org.example.kickstarterapicontract.dto.*;
import org.example.kickstarterrest.event.PledgeEventPublisher;
import org.example.kickstarterrest.exception.ResourceNotFoundException;
import org.example.kickstarterrest.storage.InMemoryStorage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PledgeService {
    private final InMemoryStorage storage;

    @Lazy
    private final ProjectService projectService;

    @Lazy
    private final RewardService rewardService;

    private final UserService userService;
    private final PledgeEventPublisher eventPublisher;

    public PledgeResponse findById(Long id) {
        if (!storage.pledges.containsKey(id)) throw new ResourceNotFoundException("Взнос", id);
        return storage.pledges.get(id);
    }

    public PagedResponse<PledgeResponse> findAllPledges(Long projectId, int page, int size) {
        Stream<PledgeResponse> stream = storage.pledges.values().stream()
                .sorted(Comparator.comparing(PledgeResponse::getPledgeId));

        if (projectId != null) {
            stream = stream.filter(p -> projectId.equals(p.getProjectId()));
        }

        List<PledgeResponse> allPledges = stream.toList();
        int totalElements = allPledges.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        int from = page * size;
        int to = Math.min(from + size, totalElements);
        List<PledgeResponse> content = (from >= totalElements) ? List.of() : allPledges.subList(from, to);

        return new PagedResponse<>(content, page, size, totalElements, totalPages, page >= totalPages - 1);
    }

    public PledgeResponse create(PledgeRequest request) {
        userService.findById(request.userId());
        ProjectResponse project = projectService.findById(request.projectId());
        RewardResponse reward = rewardService.findRewardById(request.rewardId());

        if (request.pledge().compareTo(reward.getMinPrice()) < 0) {
            throw new IllegalArgumentException("Сумма взноса меньше минимальной цены вознаграждения!");
        }

        Long id = storage.pledgeSequence.incrementAndGet();
        PledgeResponse pledge = PledgeResponse.builder()
                .pledgeId(id)
                .projectId(project.getId())
                .rewardId(reward.getId())
                .amount(request.pledge())
                .status(PledgeStatus.AUTHORIZED)
                .transactionDate(OffsetDateTime.now())
                .userId(request.userId())
                .build();

        storage.pledges.put(id, pledge);

        projectService.addPledgedAmount(project.getId(), request.pledge());
        eventPublisher.publishCreated(pledge);
        return pledge;
    }
}
