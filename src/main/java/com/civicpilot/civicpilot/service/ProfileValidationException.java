package com.civicpilot.civicpilot.service;

import java.util.List;

public class ProfileValidationException extends RuntimeException {
    private final List<String> errors;

    public ProfileValidationException(List<String> errors) {
        super("Invalid profile answers");
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}