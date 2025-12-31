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
        try {
            Trip trip = tripService.createTrip(tripDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Created trip: " + trip.getId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to create trip:\n" + e);
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<String> deleteTrip(@RequestBody IdDto idDto) {
        try {
            tripService.deleteTrip(idDto.id());
            return ResponseEntity.ok("Successfully deleted Trip");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to delete trip:\n" + e);
        }
    }

    @PostMapping("/edit-catches")
    public ResponseEntity<String> editCatches(@RequestBody EditCatchesDto editCatchesDto) {
        try {
            tripService.editCatches(editCatchesDto);
            return ResponseEntity.ok()
                .body("Successfully edited catches of trip: " + editCatchesDto.tripId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Failed to edit catches of trip:\n" + e);
        }
    }

    @PostMapping("/get-catches")
    public ResponseEntity<?> getAllCatches(@RequestBody IdDto tripIdDto) {
        try {
            AllCatchesDto catches = tripService.getAllCatches(tripIdDto.id());
            return ResponseEntity.ok().body(catches);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Failed to get all catches:\n" + e);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> listAllTrips() {
        try {
            List<TripReturnDto> trips = tripService.listAllTrips();
            return ResponseEntity.ok().body(trips);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Failed to get all catches:\n" + e);
        }
    }
}
