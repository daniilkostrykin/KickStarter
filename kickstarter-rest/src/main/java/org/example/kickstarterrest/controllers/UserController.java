package org.example.kickstarterrest.controllers;

import lombok.RequiredArgsConstructor;
import org.example.kickstarterapicontract.dto.PatchUserRequest;
import org.example.kickstarterapicontract.dto.UserRequest;
import org.example.kickstarterapicontract.endpoints.UserApi;
import org.example.kickstarterrest.assembler.UserModelAssembler;
import org.example.kickstarterrest.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.example.kickstarterapicontract.dto.UserResponse;
import org.example.kickstarterapicontract.dto.PagedResponse;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;
    private final UserModelAssembler userModelAssembler;
    private final PagedResourcesAssembler<UserResponse> pagedUsersAssembler;

    @Override
    public ResponseEntity<EntityModel<UserResponse>> createUser(UserRequest userRequest) {
        return null;
    }

    @Override
    public EntityModel<UserResponse> getUserById(Long id) {
        return null;
    }

    @Override
    public PagedModel<EntityModel<UserResponse>> getAllUsers(int page, int size) {

        PagedResponse<UserResponse> paged = userService.findAll(null, null, page, size);

        Page<UserResponse> springPage = new PageImpl<>(
                paged.content(),
                PageRequest.of(paged.pageNumber(), paged.pageSize()),
                paged.totalElements()
        );

        return pagedUsersAssembler.toModel(springPage, userModelAssembler);
    }

    @Override
    public EntityModel<UserResponse> patchUser(Long id, PatchUserRequest request) {
        return null;
    }
}