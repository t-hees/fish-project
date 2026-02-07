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
import com.tadeo.fish_project.dto.AllCatchesDto;
import com.tadeo.fish_project.dto.EditCatchesDto;
import com.tadeo.fish_project.dto.IdDto;
import com.tadeo.fish_project.dto.TripDto;
import com.tadeo.fish_project.dto.TripReturnDto;

@RestController
@RequestMapping("/api/trip")
public class TripController {
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
    public ResponseEntity<List<TripReturnDto>> listAllTrips() {
        List<TripReturnDto> trips = tripService.listAllTrips();
        return ResponseEntity.ok().body(trips);
    }
}
