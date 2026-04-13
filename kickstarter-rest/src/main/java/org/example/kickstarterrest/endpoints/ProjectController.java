package org.example.kickstarterrest.endpoints;

import lombok.RequiredArgsConstructor;
import org.example.kickstarterapicontract.endpoints.ProjectApi;
import org.example.kickstarterrest.assembler.ProjectModelAssembler;
import org.example.kickstarterapicontract.dto.PatchProjectRequest;
import org.example.kickstarterapicontract.dto.ProjectRequest;
import org.example.kickstarterapicontract.dto.ProjectResponse;
import org.example.kickstarterrest.service.ProjectService;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
public class ProjectController implements ProjectApi {
    private final ProjectService service;
    private final ProjectModelAssembler assembler;

    @Override
    public ResponseEntity<EntityModel<ProjectResponse>> createProject(ProjectRequest request) {
        EntityModel<ProjectResponse> model = assembler.toModel(service.create(request));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @Override
    public EntityModel<ProjectResponse> getProjectById(Long id) {
        return assembler.toModel(service.findById(id));
    }

    @Override
    public PagedModel<EntityModel<ProjectResponse>> getAllProjects(int page, int size) {
        List<ProjectResponse> all = service.findAll();
        int total = all.size();
        int from = page * size;
        List<ProjectResponse> content = from >= total ? List.of() : all.subList(from, Math.min(from + size, total));

        List<EntityModel<ProjectResponse>> models = content.stream().map(assembler::toModel).toList();
        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(size, page, total, (int) Math.ceil((double) total / size));

        PagedModel<EntityModel<ProjectResponse>> pagedModel = PagedModel.of(models, metadata);
        pagedModel.add(linkTo(methodOn(ProjectController.class).getAllProjects(page, size)).withSelfRel());
        return pagedModel;
    }

    @Override
    public EntityModel<ProjectResponse> patchProject(Long id, PatchProjectRequest request) {
        return assembler.toModel(service.patch(id, request));
    }
}