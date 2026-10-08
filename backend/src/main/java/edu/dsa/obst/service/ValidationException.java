package edu.dsa.obst.service;

import java.util.List;

/** Thrown when the user's input is invalid. Carries every message, which become a 400 response. */
public class ValidationException extends RuntimeException {

    private final List<String> errors;

    public ValidationException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
