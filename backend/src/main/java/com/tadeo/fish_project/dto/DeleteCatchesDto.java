package com.tadeo.fish_project.dto;

import java.util.List;

public record DeleteCatchesDto(Long tripId, List<Long> simpleCatches, List<Long> specialCatches){};
