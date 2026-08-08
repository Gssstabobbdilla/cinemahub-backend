package com.cinemahub.cinemahub.common.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String entityName, Long id) {
        return new ResourceNotFoundException("%s no encontrado con id: %d".formatted(entityName, id));
    }
}