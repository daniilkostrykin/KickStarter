package org.example.kickstarterapicontract.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PatchRewardRequest(
        @Schema(description = "Название награды", example = "Ранняя версия игры")
        String title,

        @Schema(description = "Описание награды", example = "Эксклюзивный контент для спонсоров проекта")
        @Size(min = 5, max = 100)
        String description,

        @Schema(description = "Минимальная цена награды", example = "50.00")
        @Positive(message = "Минимальная цена должна быть положительной")
        BigDecimal minPrice
) {
}