package com.tadeo.fish_project.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tadeo.fish_project.entity.Trip;
import com.tadeo.fish_project.service.TripService;
import com.tadeo.fish_project.dto.AllCatchesDto;
import com.tadeo.fish_project.dto.EditCatchesDto;
import com.tadeo.fish_project.dto.IdDto;
import com.tadeo.fish_project.dto.TripDto;
import com.tadeo.fish_project.dto.TripPageDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/trip")
@RequiredArgsConstructor
public class TripController {
    private static final int MAX_PAGE_SIZE = 50;

    private final TripService tripService;

    @PostMapping("/create")
    public ResponseEntity<String> createTrip(@Valid @RequestBody TripDto tripDto) {
        Trip trip = tripService.createTrip(tripDto);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body("Created trip: " + trip.getId());
    }

    @PostMapping("/delete")
    public ResponseEntity<String> deleteTrip(@Valid @RequestBody IdDto idDto) {
        tripService.deleteTrip(idDto.id());
        return ResponseEntity.ok("Successfully deleted Trip");
    }

    @PostMapping("/edit-catches")
    public ResponseEntity<String> editCatches(@Valid @RequestBody EditCatchesDto editCatchesDto) {
        tripService.editCatches(editCatchesDto);
        return ResponseEntity.ok()
            .body("Successfully edited catches of trip: " + editCatchesDto.tripId());
    }

    @PostMapping("/get-catches")
    public ResponseEntity<AllCatchesDto> getAllCatches(@Valid @RequestBody IdDto tripIdDto) {
        AllCatchesDto catches = tripService.getAllCatches(tripIdDto.id());
        return ResponseEntity.ok().body(catches);
    }

    @GetMapping("/all")
    public ResponseEntity<TripPageDto> listAllTrips(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Trip.Environment environment,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        TripPageDto trips = tripService.listAllTrips(page, Math.min(size, MAX_PAGE_SIZE), location, environment, from, to);
        return ResponseEntity.ok().body(trips);
    }
}
