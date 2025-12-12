package com.tadeo.fish_project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.tadeo.fish_project.entity.SimpleCatch;
import com.tadeo.fish_project.entity.SpecialCatch;
import com.tadeo.fish_project.entity.Trip;
import com.tadeo.fish_project.entity.User;
import com.tadeo.fish_project.dto.TripDto;
import com.tadeo.fish_project.dto.TripReturnDto;
import com.tadeo.fish_project.dto.AddCatchesDto;
import com.tadeo.fish_project.repository.TripRepository;

@Service
public class TripService {

    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private UserService userService;

    public Trip createTrip(TripDto tripDto) {
        User user = userService.getUser();
        Trip trip = Trip.builder()
            .location(tripDto.location())
            .simpleCatches(new HashSet<SimpleCatch>())
            .specialCatches(new HashSet<SpecialCatch>())
            .environment(tripDto.environment())
            .time(tripDto.time())
            .duration(Duration.ofHours(tripDto.hours()))
            .temperature(tripDto.temperature())
            .waterLevel(tripDto.waterLevel())
            .weather(tripDto.weather())
            .notes(tripDto.notes())
            .user(user)
            .build();

        tripRepository.save(trip);
        return trip;
    }

    public void addCatches(AddCatchesDto addCatchesDto) throws Exception {
        Trip trip = tripRepository.findById(addCatchesDto.tripId())
            .orElseThrow(() -> new RuntimeException("Trip not found with id: " + addCatchesDto.tripId()));
        trip.getSimpleCatches().addAll(addCatchesDto.simpleCatches());
        trip.getSpecialCatches().addAll(addCatchesDto.specialCatches());
        // This works because of persistence cascade
        tripRepository.save(trip);
    }

    public List<TripReturnDto> listAllTrips() {
        User user = userService.getUser();
        List<Trip> trips = tripRepository.findByUserOrderByTimeDesc(user);
        return trips.stream()
            .map(trip -> new TripReturnDto(
                trip.getId(),
                trip.getLocation(),
                trip.getEnvironment(),
                trip.getTime(),
                trip.getDuration(),
                trip.getTemperature(),
                trip.getWaterLevel(),
                trip.getWeather(),
                trip.getNotes()
            ))
            .collect(Collectors.toList());
    }
}
