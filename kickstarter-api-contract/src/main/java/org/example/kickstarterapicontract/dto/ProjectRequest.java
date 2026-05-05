package org.example.kickstarterapicontract.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProjectRequest(
        @Schema(description = "Название проекта", example = "GTA VII", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Название проекта не может быть пустым")
        @Size(min = 5, max = 100, message = "Название проекта должно быть от 5 до 100 символов")
        String title,

        @Schema(description = "Описание проекта", example = "Инновационная игра в жанре RPG с открытым миром", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Описание проекта не может быть пустым")
        String description,

        @Schema(description = "Цель проекта", example = "1000000000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Цель проекта не может быть пустой")
        @Positive(message = "Цель проекта должна быть больше нуля")
        BigDecimal goal,

        @Schema(description = "Дедлайн проекта", example = "2026-03-29T21:40:45.422407", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Дедлайн проекта не может быть пустым")
        @Future(message = "Дедлайн проекта должен быть в будущем")
        OffsetDateTime deadline
) {}