package com.tadeo.fish_project.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record EditCatchesDto(@NotNull Long tripId, @NotNull List<@Valid SimpleCatchDto> simpleCatches,
    @NotNull List<@Valid SpecialCatchDto> newSpecialCatches,
    @NotNull List<Long> removableSpecialCatchIds){};
