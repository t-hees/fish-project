package com.tadeo.fish_project.dto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

import com.tadeo.fish_project.entity.Trip;

public record TripReturnDto (Long id, String location, Trip.Environment environment, LocalDateTime time,
    Duration duration, Long temperature, Long waterLevel,
    Set<Trip.Weather> weather, String notes){};
