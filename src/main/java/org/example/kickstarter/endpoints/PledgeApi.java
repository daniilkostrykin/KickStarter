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
import org.example.kickstarter.dto.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(name = "Pledges", description = "API для управления взносами")
@RequestMapping(value = "/api/v1/pledges", produces = APPLICATION_JSON_VALUE)
public interface PledgeApi {

    @Operation(summary = "Создать новый взнос", security = @SecurityRequirement(name = KickStarterApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "201", description = "Взнос успешно создан")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации данных",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    ResponseEntity<EntityModel<PledgeResponse>> createPledge(@Valid @RequestBody PledgeRequest request);

    @Operation(summary = "Получить взнос по ID")
    @ApiResponse(responseCode = "200", description = "Взнос найден")
    @ApiResponse(responseCode = "404", description = "Взнос не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    EntityModel<PledgeResponse> getPledgeById(@Parameter(description = "Уникальный идентификатор взноса")
                                              @PathVariable Long id);

    @Operation(summary = "Получить список всех взносов")
    @ApiResponse(responseCode = "200", description = "Список взносов успешно получен")
    @GetMapping
    PagedModel<EntityModel<PledgeResponse>> getAllPledges(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size);
}