package com.tadeo.fish_project.exception;

public class FishNotFoundException extends EntityNotFoundException {
    public FishNotFoundException(Long id) {
        super("Failed to find Fish: " + String.valueOf(id));
    }
}
