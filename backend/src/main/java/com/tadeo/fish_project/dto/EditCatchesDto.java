package com.tadeo.fish_project.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/*
New special catches are created one at a time with their image, see SpecialCatchController
*/
public record EditCatchesDto(@NotNull List<@Valid SimpleCatchDto> simpleCatches,
    @NotNull List<Long> removableSpecialCatchIds){};
