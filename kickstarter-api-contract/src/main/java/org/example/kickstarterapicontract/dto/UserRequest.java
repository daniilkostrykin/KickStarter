package org.example.kickstarterapicontract.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @Schema(description = "Никнейм пользователя", example = "user", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Имя пользователя не может быть пустым")
        @Size(min = 3, max = 50, message = "Имя пользователя должно содержать от 3 до 50 символов")
        String username,

        @Schema(description = "Адрес электронной почты", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Почта не может быть пустой")
        @Email(message = "Некорректный формат электронной почты")
        String email
) {}