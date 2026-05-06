package org.example.kickstarterrest.assembler;

import org.example.kickstarterapicontract.dto.UserResponse;
import org.example.kickstarterrest.controllers.UserController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UserModelAssembler implements RepresentationModelAssembler<UserResponse, EntityModel<UserResponse>> {

    @Override
    public EntityModel<UserResponse> toModel(UserResponse entity) {
        return EntityModel.of(entity,
                linkTo(methodOn(UserController.class).getUserById(entity.getId())).withSelfRel(),
                linkTo(methodOn(UserController.class).getAllUsers(0, 10)).withRel("collection"),
                linkTo(methodOn(UserController.class).patchUser(entity.getId(), null)).withRel("update")
        );
    }
}