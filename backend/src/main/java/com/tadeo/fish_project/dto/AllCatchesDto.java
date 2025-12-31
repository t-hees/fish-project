package com.tadeo.fish_project.dto;

import java.util.List;

public record AllCatchesDto(List<SimpleCatchDto> simpleCatches, List<SpecialCatchWithIdDto> specialCatches){};
