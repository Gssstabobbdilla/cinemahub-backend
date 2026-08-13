package com.cinemahub.cinemahub.common.exception;

/**
 * Se lanza al intentar crear o actualizar una entidad violando una restricción de unicidad
 * a nivel de negocio (ej. email, name, code ya existentes). Se resuelve más adelante en un
 * @ControllerAdvice global como HTTP 409 (Conflict).
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public static DuplicateResourceException of(String entityName, String field, String value) {
        return new DuplicateResourceException(
                "Ya existe %s con %s: '%s'".formatted(entityName, field, value));
    }
}