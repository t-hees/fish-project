package com.tadeo.fish_project.controller;

import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("/api/trip")
public class TripController {
    private static final int MAX_PAGE_SIZE = 50;

    @Autowired
    private TripService tripService;

    @PostMapping("/create")
    public ResponseEntity<String> createTrip(@RequestBody TripDto tripDto) {
        Trip trip = tripService.createTrip(tripDto);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body("Created trip: " + trip.getId());
    }

    @PostMapping("/delete")
    public ResponseEntity<String> deleteTrip(@RequestBody IdDto idDto) {
        tripService.deleteTrip(idDto.id());
        return ResponseEntity.ok("Successfully deleted Trip");
    }

    @PostMapping("/edit-catches")
    public ResponseEntity<String> editCatches(@RequestBody EditCatchesDto editCatchesDto) {
        tripService.editCatches(editCatchesDto);
        return ResponseEntity.ok()
            .body("Successfully edited catches of trip: " + editCatchesDto.tripId());
    }

    @PostMapping("/get-catches")
    public ResponseEntity<AllCatchesDto> getAllCatches(@RequestBody IdDto tripIdDto) {
        AllCatchesDto catches = tripService.getAllCatches(tripIdDto.id());
        return ResponseEntity.ok().body(catches);
    }

    @GetMapping("/all")
    public ResponseEntity<TripPageDto> listAllTrips(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        TripPageDto trips = tripService.listAllTrips(page, Math.min(size, MAX_PAGE_SIZE));
        return ResponseEntity.ok().body(trips);
    }
}
