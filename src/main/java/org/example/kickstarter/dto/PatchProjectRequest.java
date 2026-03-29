package org.example.kickstarter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.kickstarter.validation.ValidProjectStatus;

public record PatchProjectRequest(

        @Schema(description = "Название проекта", example = "Игра Герои Меча и Магии")
        String title,
        
        @Schema(description = "Описание проекта", example = "Инновационная игра в жанре RPG с открытым миром")
        String description,

        @Schema(description = "Статус проекта", example = "ACTIVE")
        @ValidProjectStatus
        String status
) {
}