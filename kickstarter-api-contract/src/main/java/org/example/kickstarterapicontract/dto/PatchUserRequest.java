package org.example.kickstarterapicontract.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record PatchUserRequest(
        @Schema(description = "Имя пользователя (никнейм)", example = "danya_dev")
        @Size(min = 3, max = 50, message = "Имя пользователя должно быть от 3 до 50 символов")
        String username,

        @Schema(description = "Электронная почта пользователя", example = "danya@rut-miit.ru")
        @Email(message = "Некорректный формат электронной почты")
        String email
) {
}