package com.tadeo.fish_project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

import com.tadeo.fish_project.entity.SimpleCatch;
import com.tadeo.fish_project.entity.SpecialCatch;
import com.tadeo.fish_project.entity.Trip;
import com.tadeo.fish_project.entity.User;
import com.tadeo.fish_project.dto.TripDto;
import com.tadeo.fish_project.dto.CatchesDto;
import com.tadeo.fish_project.repository.TripRepository;

@Service
public class TripService {

    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private UserService userService;

    public Trip createTrip(TripDto tripDto) {
        Trip trip = Trip.builder()
            .location(tripDto.location())
            .simpleCatches(new HashSet<SimpleCatch>())
            .specialCatches(new HashSet<SpecialCatch>())
            .environment(tripDto.environment())
            .time(tripDto.time())
            .duration(tripDto.duration())
            .temperature(tripDto.temperature())
            .waterLevel(tripDto.waterLevel())
            .weather(tripDto.weather())
            .notes(tripDto.notes())
            .build();

        tripRepository.save(trip);
        return trip;
    }

    public void addCatches(Trip trip, CatchesDto catchesDto) {
        trip.getSimpleCatches().addAll(catchesDto.simpleCatches());
        trip.getSpecialCatches().addAll(catchesDto.specialCatches());
        // This works because of persistence cascade
        tripRepository.save(trip);
    }

    public List<Trip> listAllTrips() {
        User user = userService.getUser();
        return tripRepository.findByUserOrderByTimeDesc(user);
    }
}
