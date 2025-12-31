package com.tadeo.fish_project.dto;

import java.util.List;

public record EditCatchesDto(Long tripId, List<SimpleCatchDto> simpleCatches,
    List<SpecialCatchDto> newSpecialCatches,
    List<Long> removableSpecialCatchIds){};
