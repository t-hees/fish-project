package com.tadeo.fish_project.dto;

import java.util.List;

public record TripPageDto(List<TripReturnDto> trips, boolean hasNext, long totalElements){};
