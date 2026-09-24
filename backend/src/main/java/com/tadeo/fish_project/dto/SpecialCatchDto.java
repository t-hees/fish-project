package com.tadeo.fish_project.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SpecialCatchDto(@NotNull Long fishId, String imageData, @PositiveOrZero Long size,
    @PositiveOrZero Long weight, @Size(max = 255) String notes, String name){};
