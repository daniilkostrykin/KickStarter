package org.example.kickstarterapicontract.endpoints;

import org.example.kickstarterapicontract.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.kickstarterapicontract.config.KickStarterApiContractConfig;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "Users", description = "API для управления пользователями (спонсорами и авторами)")
@RequestMapping(value = "/api/users", produces = APPLICATION_JSON_VALUE)
public interface UserApi {

    @Operation(summary = "Создать нового пользователя")
    @ApiResponse(responseCode = "201", description = "Пользователь успешно создан")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации данных",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    ResponseEntity<EntityModel<UserResponse>> createUser(@Valid @RequestBody UserRequest userRequest);

    @Operation(summary = "Получить пользователя по ID")
    @ApiResponse(responseCode = "200", description = "Пользователь найден")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    EntityModel<UserResponse> getUserById(@Parameter(description = "ID пользователя") @PathVariable Long id);


    @Operation(summary = "Получить список всех пользователей")
    @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен")
    @GetMapping
    PagedModel<EntityModel<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );

    @Operation(
            summary = "Частично изменить данные пользователя",
            security = @SecurityRequirement(name = KickStarterApiContractConfig.SECURITY_SCHEME_BEARER)
    )
    @ApiResponse(responseCode = "200", description = "Данные пользователя обновлены")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Пользователь не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    EntityModel<UserResponse> patchUser(
            @Parameter(description = "ID пользователя", required = true, example = "1") @PathVariable Long id,
            @Valid @RequestBody PatchUserRequest request
    );
}