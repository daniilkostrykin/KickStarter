package org.example.kickstarter.endpoints;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.kickstarter.config.KickStarterApiContractConfig;
import org.example.kickstarter.dto.ErrorResponse;
import org.example.kickstarter.dto.PatchProjectRequest;
import org.example.kickstarter.dto.ProjectRequest;
import org.example.kickstarter.dto.ProjectResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(name = "Projects", description = "API для управления проектами")
@RequestMapping(value = "/api/v1/projects",  produces = APPLICATION_JSON_VALUE)
public interface ProjectApi {

    @Operation(
            summary = "Создать новый проект",
            description = "Создает черновик проекта и возвращает его данные со ссылками",
            security = @SecurityRequirement(name = KickStarterApiContractConfig.SECURITY_SCHEME_BEARER)
    )
    @ApiResponse(responseCode = "201", description = "Проект успешно создан")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации данных",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    ResponseEntity<EntityModel<ProjectResponse>> createProject(
            @Valid @RequestBody ProjectRequest request
    );

    @Operation(summary = "Получить проект по ID")
    @ApiResponse(responseCode = "200", description = "Проект найден")
    @ApiResponse(responseCode = "404", description = "Проект не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    EntityModel<ProjectResponse> getProjectById(@Parameter(description = "Уникальный идентификатор проекта")
                                                @PathVariable Long id);

    @Operation(summary = "Получить список всех проектов")
    @ApiResponse(responseCode = "200", description = "Список проектов успешно получен")
    @GetMapping
    PagedModel<EntityModel<ProjectResponse>> getAllProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );

    @Operation(summary = "Изменить проект",
            security = @SecurityRequirement(name = KickStarterApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "200", description = "Проект обновлён")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Проект не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PatchMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
    EntityModel<ProjectResponse> patchProject(
            @PathVariable Long id,
            @Valid @RequestBody PatchProjectRequest request
    );
}