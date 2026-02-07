package com.tadeo.fish_project.exception;

public class TripNotFoundException extends EntityNotFoundException {
    public TripNotFoundException(Long id) {
        super("Failed to find Trip: " + String.valueOf(id));
    }
}
