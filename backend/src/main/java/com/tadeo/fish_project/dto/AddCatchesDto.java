package com.tadeo.fish_project.dto;

import java.util.List;

public record AddCatchesDto(Long tripId, List<SimpleCatchDto> simpleCatches, List<SpecialCatchDto> specialCatches){};
