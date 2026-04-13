package org.example.kickstarterrest.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " с ID '" + id + "' не найден");
    }
}