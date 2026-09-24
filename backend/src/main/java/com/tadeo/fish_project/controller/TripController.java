package com.tadeo.fish_project.controller;

import java.net.URI;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tadeo.fish_project.entity.Trip;
import com.tadeo.fish_project.service.TripService;
import com.tadeo.fish_project.dto.AllCatchesDto;
import com.tadeo.fish_project.dto.EditCatchesDto;
import com.tadeo.fish_project.dto.TripDto;
import com.tadeo.fish_project.dto.TripPageDto;
import com.tadeo.fish_project.dto.TripReturnDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {
    private static final int MAX_PAGE_SIZE = 50;

    private final TripService tripService;

    @GetMapping
    public ResponseEntity<TripPageDto> listTrips(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Trip.Environment environment,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        TripPageDto trips = tripService.listAllTrips(page, Math.min(size, MAX_PAGE_SIZE), location, environment, from, to);
        return ResponseEntity.ok(trips);
    }

    @PostMapping
    public ResponseEntity<TripReturnDto> createTrip(@Valid @RequestBody TripDto tripDto) {
        TripReturnDto trip = tripService.createTrip(tripDto);
        return ResponseEntity.created(URI.create("/api/trips/" + trip.id())).body(trip);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(@PathVariable Long id) {
        tripService.deleteTrip(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/catches")
    public ResponseEntity<AllCatchesDto> getCatches(@PathVariable Long id) {
        return ResponseEntity.ok(tripService.getAllCatches(id));
    }

    /*
    Replaces the simple catches, adds and removes special catches and returns the resulting catches
    */
    @PutMapping("/{id}/catches")
    public ResponseEntity<AllCatchesDto> editCatches(@PathVariable Long id,
            @Valid @RequestBody EditCatchesDto editCatchesDto) {
        tripService.editCatches(id, editCatchesDto);
        return ResponseEntity.ok(tripService.getAllCatches(id));
    }
}
