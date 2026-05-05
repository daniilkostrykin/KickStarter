package org.example.kickstarterrest.controllers;

import lombok.RequiredArgsConstructor;
import org.example.kickstarterapicontract.dto.PagedResponse;
import org.example.kickstarterapicontract.endpoints.RewardApi;
import org.example.kickstarterrest.assembler.RewardModelAssembler;
import org.example.kickstarterapicontract.dto.PatchRewardRequest;
import org.example.kickstarterapicontract.dto.RewardRequest;
import org.example.kickstarterapicontract.dto.RewardResponse;
import org.example.kickstarterrest.service.RewardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RewardController implements RewardApi {
    private final RewardService rewardService;
    private final RewardModelAssembler rewardModelAssembler;
    private final PagedResourcesAssembler<RewardResponse> pagedRewardsAssembler;

    @Override
    public ResponseEntity<EntityModel<RewardResponse>> createReward(RewardRequest request) {
        EntityModel<RewardResponse> model = rewardModelAssembler.toModel(rewardService.createReward(request));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @Override
    public EntityModel<RewardResponse> getRewardById(Long id) {
        return rewardModelAssembler.toModel(rewardService.findRewardById(id));
    }

    @Override
    public PagedModel<EntityModel<RewardResponse>> getAllRewards(int page, int size) {
        PagedResponse<RewardResponse> paged = rewardService.findAllRewards(null, null, null, page, size);
        Page<RewardResponse> springPage = new PageImpl<>(
                paged.content(),
                PageRequest.of(paged.pageNumber(), paged.pageSize()),
                paged.totalElements()
        );
        return pagedRewardsAssembler.toModel(springPage, rewardModelAssembler);
    }

    @Override
    public EntityModel<RewardResponse> patchReward(Long id, PatchRewardRequest request) {
        return rewardModelAssembler.toModel(rewardService.patchReward(id, request));
    }
}