package org.example.kickstarterrest.controllers;

import lombok.RequiredArgsConstructor;
import org.example.kickstarterapicontract.endpoints.RewardApi;
import org.example.kickstarterrest.assembler.RewardModelAssembler;
import org.example.kickstarterapicontract.dto.PatchRewardRequest;
import org.example.kickstarterapicontract.dto.RewardRequest;
import org.example.kickstarterapicontract.dto.RewardResponse;
import org.example.kickstarterrest.service.RewardService;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
public class RewardController implements RewardApi {
    private final RewardService service;
    private final RewardModelAssembler assembler;

    @Override
    public ResponseEntity<EntityModel<RewardResponse>> createReward(RewardRequest request) {
        EntityModel<RewardResponse> model = assembler.toModel(service.create(request));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @Override
    public EntityModel<RewardResponse> getRewardById(Long id) {
        return assembler.toModel(service.findById(id));
    }

    @Override
    public PagedModel<EntityModel<RewardResponse>> getAllRewards(int page, int size) {
        List<RewardResponse> all = service.findAll();
        int total = all.size();
        int from = page * size;
        List<RewardResponse> content = from >= total ? List.of() : all.subList(from, Math.min(from + size, total));

        List<EntityModel<RewardResponse>> models = content.stream().map(assembler::toModel).toList();
        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(size, page, total, (int) Math.ceil((double) total / size));

        PagedModel<EntityModel<RewardResponse>> pagedModel = PagedModel.of(models, metadata);
        pagedModel.add(linkTo(methodOn(RewardController.class).getAllRewards(page, size)).withSelfRel());
        return pagedModel;
    }

    @Override
    public EntityModel<RewardResponse> patchReward(Long id, PatchRewardRequest request) {
        return assembler.toModel(service.patch(id, request));
    }
}