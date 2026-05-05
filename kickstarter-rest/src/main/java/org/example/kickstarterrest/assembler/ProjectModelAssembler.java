package org.example.kickstarterrest.assembler;

import org.example.kickstarterapicontract.dto.PatchProjectRequest;
import org.example.kickstarterapicontract.dto.ProjectResponse;
import org.example.kickstarterrest.controllers.ProjectController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProjectModelAssembler implements RepresentationModelAssembler<ProjectResponse, EntityModel<ProjectResponse>> {
    @Override
    public EntityModel<ProjectResponse> toModel(ProjectResponse entity) {
        return EntityModel.of(entity,
                linkTo(methodOn(ProjectController.class).getProjectById(entity.getId())).withSelfRel(),
                linkTo(methodOn(ProjectController.class).getAllProjects(0, 10)).withRel("collection"),
                linkTo(methodOn(ProjectController.class).patchProject(entity.getId(), (PatchProjectRequest) null)).withRel("update")
        );
    }
}