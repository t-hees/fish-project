package com.tadeo.fish_project.dto;

import java.util.Optional;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SimpleCatchDto(@NotNull Long fishId, @NotNull @Positive Integer amount, Optional<String> name){};
