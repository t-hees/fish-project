package com.tadeo.fish_project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.tadeo.fish_project.entity.Trip;
import com.tadeo.fish_project.service.TripService;
import com.tadeo.fish_project.dto.AddCatchesDto;
import com.tadeo.fish_project.dto.TripDto;
import com.tadeo.fish_project.dto.TripReturnDto;

@RestController
@RequestMapping("/api/trip")
public class TripController {
    @Autowired
    private TripService tripService;

    @PostMapping("/create")
    public ResponseEntity<String> createTrip(@RequestBody TripDto tripDto) {
        try {
            Trip trip = tripService.createTrip(tripDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Created trip: " + trip.getId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to create trip:\n" + e);
        }
    }

    @PostMapping("/add-catches")
    public ResponseEntity<String> addCatches(@RequestBody AddCatchesDto addCatchesDto) {
        try {
            tripService.addCatches(addCatchesDto);
            return ResponseEntity.ok()
                    .body("Added catches to trip: " + addCatchesDto.tripId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to add catches to trip:\n" + e);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<TripReturnDto>> listAllTrips() {
        try {
            List<TripReturnDto> trips = tripService.listAllTrips();
            return ResponseEntity.ok().body(trips);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
