package com.tadeo.fish_project.dto;

import java.time.LocalDateTime;
import java.util.Set;

import com.tadeo.fish_project.entity.Trip;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record TripDto (@NotBlank @Size(max = 255) String location, Trip.Environment environment,
    @NotNull LocalDateTime time,
    @PositiveOrZero Long hours, Long temperature, Long waterLevel,
    Set<Trip.Weather> weather, @Size(max = 255) String notes){};
