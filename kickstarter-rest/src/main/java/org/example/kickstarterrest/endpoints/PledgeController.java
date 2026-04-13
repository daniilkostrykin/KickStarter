package org.example.kickstarterrest.endpoints;

import lombok.RequiredArgsConstructor;
import org.example.kickstarterapicontract.endpoints.PledgeApi;
import org.example.kickstarterrest.assembler.PledgeModelAssembler;
import org.example.kickstarterapicontract.dto.PledgeRequest;
import org.example.kickstarterapicontract.dto.PledgeResponse;
import org.example.kickstarterrest.service.PledgeService;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
public class PledgeController implements PledgeApi {
    private final PledgeService service;
    private final PledgeModelAssembler assembler;

    @Override
    public ResponseEntity<EntityModel<PledgeResponse>> createPledge(PledgeRequest request) {
        EntityModel<PledgeResponse> model = assembler.toModel(service.create(request));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @Override
    public EntityModel<PledgeResponse> getPledgeById(Long id) {
        return assembler.toModel(service.findById(id));
    }

    @Override
    public PagedModel<EntityModel<PledgeResponse>> getAllPledges(int page, int size) {
        List<PledgeResponse> all = service.findAll();
        int total = all.size();
        int from = page * size;
        List<PledgeResponse> content = from >= total ? List.of() : all.subList(from, Math.min(from + size, total));

        List<EntityModel<PledgeResponse>> models = content.stream().map(assembler::toModel).toList();
        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(size, page, total, (int) Math.ceil((double) total / size));

        PagedModel<EntityModel<PledgeResponse>> pagedModel = PagedModel.of(models, metadata);
        pagedModel.add(linkTo(methodOn(PledgeController.class).getAllPledges(page, size)).withSelfRel());
        return pagedModel;
    }
}