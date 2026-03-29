package org.example.kickstarter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RewardRequest(
    @Schema(description = "Название награды", example = "Ранняя версия игры", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Название награды не может быть пустым")
    String title,

    @Schema(description = "Описание награды", example = "Эксклюзивный контент для спонсоров проекта", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Описание награды не может быть пустым")
    @Size(min = 5, max = 100)
    String description,

    @Schema(description = "Минимальная цена награды", example = "50.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Минимальная цена не может быть пустой")
    @Positive(message = "Минимальная цена должна быть положительной")
    BigDecimal minPrice,

    @Schema(description = "ID проекта, к которому привязана награда", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "ID проекта не может быть пустым")
    Long projectId

){}