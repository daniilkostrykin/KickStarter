package org.example.kickstarterrest.controllers;

import lombok.RequiredArgsConstructor;
import org.example.kickstarterapicontract.dto.PagedResponse;
import org.example.kickstarterapicontract.dto.PledgeResponse;
import org.example.kickstarterapicontract.endpoints.PledgeApi;
import org.example.kickstarterrest.assembler.PledgeModelAssembler;
import org.example.kickstarterapicontract.dto.PledgeRequest;
import org.example.kickstarterapicontract.dto.PledgeResponse;
import org.example.kickstarterrest.service.PledgeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequiredArgsConstructor
public class PledgeController implements PledgeApi {
    private final PledgeService pledgeService;
    private final PledgeModelAssembler pledgeModelAssembler;
    private final PagedResourcesAssembler<PledgeResponse> pagedPledgesAssembler;

    @Override
    public ResponseEntity<EntityModel<PledgeResponse>> createPledge(PledgeRequest request) {
        EntityModel<PledgeResponse> model = pledgeModelAssembler.toModel(pledgeService.create(request));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @Override
    public EntityModel<PledgeResponse> getPledgeById(Long id) {
        return pledgeModelAssembler.toModel(pledgeService.findById(id));
    }

    @Override
    public PagedModel<EntityModel<PledgeResponse>> getAllPledges(int page, int size) {
        PagedResponse<PledgeResponse> paged = pledgeService.findAllPledges(null, page, size);
        Page<PledgeResponse> springPage = new PageImpl<>(
                paged.content(),
                PageRequest.of(paged.pageNumber(), paged.pageSize()),
                paged.totalElements()
        );
        return pagedPledgesAssembler.toModel(springPage, pledgeModelAssembler);
    }
}