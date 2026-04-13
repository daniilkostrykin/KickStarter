package org.example.kickstarterapicontract.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PledgeRequest(
        @Schema(description = "ID проекта", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "ID проекта не может быть пустым")
        Long projectId,

        @Schema(description = "ID вознаграждения", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "ID вознаграждения не может быть пустым")
        Long rewardId,

        @Schema(description = "Размер вознаграждения", example = "1000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Размер вознаграждения не может быть пустым")
        @Positive(message = "Вознаграждение должно быть больше 0")
        BigDecimal pledge
) {
}