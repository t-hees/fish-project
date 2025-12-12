package com.tadeo.fish_project.dto;

import java.util.List;

import com.tadeo.fish_project.entity.SimpleCatch;
import com.tadeo.fish_project.entity.SpecialCatch;

public record AddCatchesDto(Long tripId, List<SimpleCatch> simpleCatches, List<SpecialCatch> specialCatches){};
