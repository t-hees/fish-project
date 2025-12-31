package com.tadeo.fish_project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.tadeo.fish_project.entity.Fish;
import com.tadeo.fish_project.entity.SimpleCatch;
import com.tadeo.fish_project.entity.SpecialCatch;
import com.tadeo.fish_project.entity.Trip;
import com.tadeo.fish_project.entity.User;
import com.tadeo.fish_project.entity.Image;
import com.tadeo.fish_project.dto.TripDto;
import com.tadeo.fish_project.dto.TripReturnDto;
import com.tadeo.fish_project.dto.AllCatchesDto;
import com.tadeo.fish_project.dto.EditCatchesDto;
import com.tadeo.fish_project.dto.SimpleCatchDto;
import com.tadeo.fish_project.dto.SpecialCatchDto;
import com.tadeo.fish_project.dto.SpecialCatchWithIdDto;
import com.tadeo.fish_project.repository.SimpleCatchRepository;
import com.tadeo.fish_project.repository.SpecialCatchRepository;
import com.tadeo.fish_project.repository.TripRepository;

@Service
public class TripService {

    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private SpecialCatchRepository specialCatchRepository;
    @Autowired
    private SimpleCatchRepository simpleCatchRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private FishService fishSerice;

    public Trip createTrip(TripDto tripDto) {
        User user = userService.getUser();
        Trip trip = Trip.builder()
            .location(tripDto.location())
            .simpleCatches(new HashSet<SimpleCatch>())
            .specialCatches(new HashSet<SpecialCatch>())
            .environment(tripDto.environment())
            .time(tripDto.time())
            .duration((tripDto.hours() != null) ? Duration.ofHours(tripDto.hours()) : null)
            .temperature(tripDto.temperature())
            .waterLevel(tripDto.waterLevel())
            .weather(tripDto.weather())
            .notes(tripDto.notes())
            .user(user)
            .build();

        tripRepository.save(trip);
        return trip;
    }

    public void deleteTrip(Long id) throws Exception {
        Trip trip = tripRepository.findByIdAndUser(id, userService.getUser())
            .orElseThrow(() -> new RuntimeException("Failed to find Trip: " + id));
        tripRepository.delete(trip);
    }

    public void editCatches(EditCatchesDto editCatchesDto) throws Exception {
        Trip trip = tripRepository.findByIdAndUser(editCatchesDto.tripId(), userService.getUser())
            .orElseThrow(() -> new RuntimeException("Trip not found with id: " + editCatchesDto.tripId()));

        Set<SimpleCatch> simpleCatches = editCatchesDto.simpleCatches().stream().map((dto) -> {
            Fish fish = fishSerice.findById(dto.fishId())
                .orElseThrow(() -> new RuntimeException("Failed to find Fish with id: " + dto.fishId()));
            return SimpleCatch.builder()
                .fish(fish)
                .amount(dto.amount())
                .build();
        }).collect(Collectors.toSet());

        List<SpecialCatch> newSpecialCatches = editCatchesDto.newSpecialCatches().stream().map((dto) -> {
            Fish fish = fishSerice.findById(dto.fishId())
                .orElseThrow(() -> new RuntimeException("Failed to find Fish with id: " + dto.fishId()));

            Image image = null;
            if (dto.imageData() != null) {
                image = new Image();
                image.setBase64Image(dto.imageData());
            }

            return SpecialCatch.builder()
                .fish(fish)
                .image(image)
                .size(dto.size())
                .weight(dto.weight())
                .notes(dto.notes())
                .build();
        }).toList();

        for (SpecialCatch specialCatch : specialCatchRepository.findAllById(editCatchesDto.removableSpecialCatchIds())) {
            trip.getSpecialCatches().remove(specialCatch);
        }
        trip.getSimpleCatches().clear();
        trip.getSimpleCatches().addAll(simpleCatches);
        trip.getSpecialCatches().addAll(newSpecialCatches);
        // This works because of persistence cascade
        tripRepository.save(trip);
    }

    public AllCatchesDto getAllCatches(Long tripId) {
        Trip trip = tripRepository.findByIdAndUser(tripId, userService.getUser())
            .orElseThrow(() -> new RuntimeException("Failed to find Trip: " + tripId));

        List<SimpleCatchDto> simpleCatches = trip.getSimpleCatches().stream()
            .map(simpleCatch -> new SimpleCatchDto(
                simpleCatch.getFish().getId(),
                simpleCatch.getAmount(),
                Optional.of(simpleCatch.getFish().getScientificName())
            ))
            .collect(Collectors.toList());

        List<SpecialCatchWithIdDto> specialCatches = trip.getSpecialCatches().stream()
            .map(specialCatch -> new SpecialCatchWithIdDto(
                specialCatch.getId(),
                specialCatch.getFish().getId(),
                (specialCatch.getImage() != null) ? specialCatch.getImage().getBase64Image() : null,
                specialCatch.getSize(),
                specialCatch.getWeight(),
                specialCatch.getNotes(),
                specialCatch.getFish().getScientificName()
            ))
            .collect(Collectors.toList());

        return new AllCatchesDto(simpleCatches, specialCatches);
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
