package org.example.kickstarter.endpoints;

import org.example.kickstarter.dto.PatchProjectRequest;
import org.example.kickstarter.dto.ProjectRequest;
import org.example.kickstarter.dto.ProjectResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class ProjectController implements ProjectApi {

    @Override
    public ResponseEntity<EntityModel<ProjectResponse>> createProject(ProjectRequest request) {
        return null;
    }

    @Override
    public EntityModel<ProjectResponse> getProjectById(Long id) {
        ProjectResponse response = ProjectResponse.builder()
                .id(id)
                .title("Умный рюкзак")
                .description("Рюкзак со встроенным powerbank")
                .goal(new BigDecimal("500000"))
                .pledged(new BigDecimal("150000"))
                .status("ACTIVE")
                .deadline(LocalDateTime.now().plusDays(30))
                .build();

        return EntityModel.of(response,
                linkTo(methodOn(ProjectController.class).getProjectById(id)).withSelfRel()
        );
    }

    @Override
    public PagedModel<EntityModel<ProjectResponse>> getAllProjects(int page, int size) {
        return null;
    }

    @Override
    public EntityModel<ProjectResponse> patchProject(Long id, PatchProjectRequest request) {
        return null;
    }
}