package org.example.kickstarterrest.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.example.kickstarterapicontract.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.example.kickstarterapicontract.exception.ProjectNotFoundException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            ResourceNotFoundException.class,
            org.example.kickstarterapicontract.exception.ResourceNotFoundException.class,
            ProjectNotFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(RuntimeException ex, HttpServletRequest request) {
        return new ErrorResponse(404, "https://api.kickstarter.com/errors/not-found",
                "Ресурс не найден", ex.getMessage(), request.getRequestURI(), Instant.now(), null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {
        return new ErrorResponse(400, "https://api.kickstarter.com/errors/bad-request",
                "Ошибка бизнес-логики", ex.getMessage(), request.getRequestURI(), Instant.now(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErrorResponse.FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ErrorResponse.FieldError(e.getField(), e.getRejectedValue(), e.getDefaultMessage()))
                .toList();
        return new ErrorResponse(400, "https://api.kickstarter.com/errors/validation",
                "Ошибка валидации", "Неверный формат данных запроса", request.getRequestURI(), Instant.now(), errors);
    }


    @ExceptionHandler({
            DuplicateResourceException.class,
            org.springframework.dao.DataIntegrityViolationException.class
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflict(Exception ex, HttpServletRequest request) {
        return new ErrorResponse(409, "https://api.kickstarter.com/errors/conflict",
                "Конфликт данных", ex.getMessage(), request.getRequestURI(), Instant.now(), null);
    }
}
