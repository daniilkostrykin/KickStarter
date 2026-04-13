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

@Tag(name = "Rewards", description = "API для управления вознаграждениями")
@RequestMapping(value = "/api/rewards", produces = APPLICATION_JSON_VALUE)
public interface RewardApi {

    @Operation(
            summary = "Создать новое вознаграждение", security = @SecurityRequirement(name = KickStarterApiContractConfig.SECURITY_SCHEME_BEARER)
    )
    @ApiResponse(responseCode = "201", description = "Вознаграждение успешно создано")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации данных",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    ResponseEntity<EntityModel<RewardResponse>> createReward(@Valid @RequestBody RewardRequest rewardRequest);

    @Operation(summary = "Получить вознаграждение по ID")
    @ApiResponse(responseCode = "200", description = "Вознаграждение найдено")
    @ApiResponse(responseCode = "404", description = "Вознаграждение не найдено",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    EntityModel<RewardResponse> getRewardById(@Parameter(description = "ID вознаграждения") @PathVariable Long id);


    @Operation(summary = "Получить список всех вознаграждений")
    @ApiResponse(responseCode = "200", description = "Список вознаграждений успешно получен")
    @GetMapping
    PagedModel<EntityModel<RewardResponse>> getAllRewards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );

    @Operation(summary = "Частично изменить вознаграждение", security = @SecurityRequirement(name = KickStarterApiContractConfig.SECURITY_SCHEME_BEARER))
    @ApiResponse(responseCode = "200", description = "Вознаграждение обновлено")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Вознаграждение не найдено",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    EntityModel<RewardResponse> patchReward(
            @Parameter(description = "ID вознаграждения", required = true, example = "1") @PathVariable Long id,
            @Valid @RequestBody PatchRewardRequest request
    );
}