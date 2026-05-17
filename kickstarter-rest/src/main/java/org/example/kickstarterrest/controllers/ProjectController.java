package org.example.kickstarterrest.controllers;

import lombok.RequiredArgsConstructor;
import org.example.kickstarterapicontract.dto.PagedResponse;
import org.example.kickstarterapicontract.endpoints.ProjectApi;
import org.example.kickstarterrest.assembler.ProjectModelAssembler;
import org.example.kickstarterapicontract.dto.PatchProjectRequest;
import org.example.kickstarterapicontract.dto.ProjectRequest;
import org.example.kickstarterapicontract.dto.ProjectResponse;
import org.example.kickstarterrest.service.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProjectController implements ProjectApi {
    private final ProjectService projectService;
    private final ProjectModelAssembler projectModelAssembler;
    private final PagedResourcesAssembler<ProjectResponse> pagedProjectsAssembler;

    @Override
    public ResponseEntity<EntityModel<ProjectResponse>> createProject(ProjectRequest request) {
        EntityModel<ProjectResponse> model = projectModelAssembler.toModel(projectService.create(request));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @Override
    public EntityModel<ProjectResponse> getProjectById(Long id) {
        return projectModelAssembler.toModel(projectService.findById(id));
    }

    @Override
    public PagedModel<EntityModel<ProjectResponse>> getAllProjects(int page, int size) {
        PagedResponse<ProjectResponse> paged = projectService.findAll(page, size);
        Page<ProjectResponse> springPage = new PageImpl<>(
                paged.content(),
                PageRequest.of(paged.pageNumber(), paged.pageSize()),
                paged.totalElements()
        );
        return pagedProjectsAssembler.toModel(springPage, projectModelAssembler);
    }

    @Override
    public EntityModel<ProjectResponse> patchProject(Long id, PatchProjectRequest request) {
        return projectModelAssembler.toModel(projectService.patch(id, request));
    }

    @Override
    public ResponseEntity<Void> deleteProject(Long id) {
        projectService.delete(id);

        return ResponseEntity.noContent().build();
    }
}