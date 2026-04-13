package org.example.kickstarterrest.assembler;


import org.example.kickstarterapicontract.dto.PledgeResponse;
import org.example.kickstarterrest.endpoints.PledgeController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PledgeModelAssembler implements RepresentationModelAssembler<PledgeResponse, EntityModel<PledgeResponse>> {
    @Override
    public EntityModel<PledgeResponse> toModel(PledgeResponse entity) {
        return EntityModel.of(entity,
                linkTo(methodOn(PledgeController.class).getPledgeById(entity.getPledgeId())).withSelfRel(),
                linkTo(methodOn(PledgeController.class).getAllPledges(0, 10)).withRel("collection")
        );
    }
}