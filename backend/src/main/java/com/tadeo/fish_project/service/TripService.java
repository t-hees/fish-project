package com.tadeo.fish_project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.tadeo.fish_project.entity.Fish;
import com.tadeo.fish_project.entity.SimpleCatch;
import com.tadeo.fish_project.entity.SpecialCatch;
import com.tadeo.fish_project.entity.Trip;
import com.tadeo.fish_project.entity.User;
import com.tadeo.fish_project.entity.Image.ImageType;
import com.tadeo.fish_project.entity.Image;
import com.tadeo.fish_project.dto.TripDto;
import com.tadeo.fish_project.dto.TripReturnDto;
import com.tadeo.fish_project.dto.AddCatchesDto;
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

    public void addCatches(AddCatchesDto addCatchesDto) throws Exception {
        Trip trip = tripRepository.findByIdAndUser(addCatchesDto.tripId(), userService.getUser())
            .orElseThrow(() -> new RuntimeException("Trip not found with id: " + addCatchesDto.tripId()));

        List<SimpleCatch> simpleCatches = addCatchesDto.simpleCatches().stream().map((dto) -> {
            Fish fish = fishSerice.findById(dto.fishId())
                .orElseThrow(() -> new RuntimeException("Failed to find Fish with id: " + dto.fishId()));
            return SimpleCatch.builder()
                .fish(fish)
                .amount(dto.amount())
                .build();
        }).toList();

        List<SpecialCatch> specialCatches = addCatchesDto.specialCatches().stream().map((dto) -> {
            Fish fish = fishSerice.findById(dto.fishId())
                .orElseThrow(() -> new RuntimeException("Failed to find Fish with id: " + dto.fishId()));

            Image image = null;
            if (dto.imageData() != null) {
                ImageType imageType = Image.getImageType(dto.imageData());
                if (imageType == ImageType.UNKNOWN) throw new RuntimeException("Unknown Image Type");
                image = Image.builder()
                    .data(dto.imageData())
                    .type(imageType)
                    .build();
            }

            return SpecialCatch.builder()
                .fish(fish)
                .image(image)
                .size(dto.size())
                .weight(dto.weight())
                .notes(dto.notes())
                .build();
        }).toList();

        trip.getSimpleCatches().addAll(simpleCatches);
        trip.getSpecialCatches().addAll(specialCatches);
        // This works because of persistence cascade
        tripRepository.save(trip);
    }

    public void deleteCatches(Long tripId, List<Long> simpleCatchIds, List<Long> specialCatchIds) throws Exception {
        Trip trip = tripRepository.findByIdAndUser(tripId, userService.getUser())
            .orElseThrow(() -> new RuntimeException("Failed to find Trip: " + tripId));

        Set<SimpleCatch> simpleCatches = trip.getSimpleCatches();
        for (SimpleCatch simpleCatch : simpleCatchRepository.findAllById(simpleCatchIds)) {
            simpleCatches.remove(simpleCatch);
        }
        Set<SpecialCatch> specialCatches = trip.getSpecialCatches();
        for (SpecialCatch specialCatch : specialCatchRepository.findAllById(specialCatchIds)) {
            specialCatches.remove(specialCatch);
        }

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
