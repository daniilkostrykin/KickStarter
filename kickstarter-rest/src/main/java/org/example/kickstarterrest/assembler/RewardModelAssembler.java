package org.example.kickstarterrest.assembler;

import org.example.kickstarterapicontract.dto.RewardResponse;
import org.example.kickstarterrest.controllers.RewardController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class RewardModelAssembler implements RepresentationModelAssembler<RewardResponse, EntityModel<RewardResponse>> {
    @Override
    public EntityModel<RewardResponse> toModel(RewardResponse entity) {
        return EntityModel.of(entity,
                linkTo(methodOn(RewardController.class).getRewardById(entity.getId())).withSelfRel(),
                linkTo(methodOn(RewardController.class).getAllRewards(0, 10)).withRel("collection"),
                linkTo(methodOn(RewardController.class).patchReward(entity.getId(), null)).withRel("update")
        );
    }
}
