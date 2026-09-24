package com.tadeo.fish_project.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
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
import com.tadeo.fish_project.exception.FishNotFoundException;
import com.tadeo.fish_project.exception.ImageNotFoundException;
import com.tadeo.fish_project.exception.TripNotFoundException;
import com.tadeo.fish_project.entity.Image;
import com.tadeo.fish_project.dto.SpecialCatchDto;
import com.tadeo.fish_project.dto.TripDto;
import com.tadeo.fish_project.dto.TripPageDto;
import com.tadeo.fish_project.dto.TripReturnDto;
import com.tadeo.fish_project.dto.AllCatchesDto;
import com.tadeo.fish_project.dto.EditCatchesDto;
import com.tadeo.fish_project.dto.SimpleCatchDto;
import com.tadeo.fish_project.dto.SpecialCatchWithIdDto;
import com.tadeo.fish_project.repository.SpecialCatchRepository;
import com.tadeo.fish_project.repository.TripRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final SpecialCatchRepository specialCatchRepository;
    private final UserService userService;
    private final FishService fishSerice;
    private final ImageService imageService;

    public TripReturnDto createTrip(TripDto tripDto) {
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
            .weather((tripDto.weather() != null) ? new HashSet<>(tripDto.weather()) : new HashSet<>())
            .notes(tripDto.notes())
            .user(user)
            .build();

        return toReturnDto(tripRepository.save(trip));
    }

    public void deleteTrip(Long id) {
        Trip trip = tripRepository.findByIdAndUser(id, userService.getUser())
            .orElseThrow(() -> new TripNotFoundException(id));
        tripRepository.delete(trip);
    }

    public void editCatches(Long tripId, EditCatchesDto editCatchesDto) {
        Trip trip = tripRepository.findByIdAndUser(tripId, userService.getUser())
            .orElseThrow(() -> new TripNotFoundException(tripId));

        Set<SimpleCatch> simpleCatches = editCatchesDto.simpleCatches().stream().map((dto) -> {
            Fish fish = fishSerice.findById(dto.fishId())
                .orElseThrow(() -> new FishNotFoundException(dto.fishId()));
            return SimpleCatch.builder()
                .fish(fish)
                .amount(dto.amount())
                .build();
        }).collect(Collectors.toSet());

        for (SpecialCatch specialCatch : specialCatchRepository.findAllById(editCatchesDto.removableSpecialCatchIds())) {
            trip.getSpecialCatches().remove(specialCatch);
        }
        trip.getSimpleCatches().clear();
        trip.getSimpleCatches().addAll(simpleCatches);
        // This works because of persistence cascade
        tripRepository.save(trip);
    }

    /*
    image may be null for a catch without image
    */
    public SpecialCatchWithIdDto createSpecialCatch(Long tripId, SpecialCatchDto dto, MultipartFile image) {
        Trip trip = findOwnTrip(tripId);
        Fish fish = fishSerice.findById(dto.fishId())
            .orElseThrow(() -> new FishNotFoundException(dto.fishId()));

        SpecialCatch specialCatch = specialCatchRepository.save(SpecialCatch.builder()
            .fish(fish)
            .image((image != null && !image.isEmpty()) ? imageService.createImage(image) : null)
            .size(dto.size())
            .weight(dto.weight())
            .notes(dto.notes())
            .build());
        trip.getSpecialCatches().add(specialCatch);
        tripRepository.save(trip);
        return toSpecialCatchDto(tripId, specialCatch);
    }

    /*
    The catch has to belong to a trip of the authenticated user
    */
    public Image getSpecialCatchImage(Long tripId, Long catchId) {
        return findOwnTrip(tripId).getSpecialCatches().stream()
            .filter(specialCatch -> specialCatch.getId().equals(catchId))
            .findFirst()
            .map(SpecialCatch::getImage)
            .orElseThrow(() -> new ImageNotFoundException(catchId));
    }

    public AllCatchesDto getAllCatches(Long tripId) {
        Trip trip = findOwnTrip(tripId);

        List<SimpleCatchDto> simpleCatches = trip.getSimpleCatches().stream()
            .map(simpleCatch -> new SimpleCatchDto(
                simpleCatch.getFish().getId(),
                simpleCatch.getAmount(),
                Optional.of(simpleCatch.getFish().getScientificName())
            ))
            .collect(Collectors.toList());

        List<SpecialCatchWithIdDto> specialCatches = trip.getSpecialCatches().stream()
            .sorted(Comparator.comparing(SpecialCatch::getId))
            .map(specialCatch -> toSpecialCatchDto(tripId, specialCatch))
            .collect(Collectors.toList());

        return new AllCatchesDto(simpleCatches, specialCatches);
    }

    public TripPageDto listAllTrips(int page, int size, String location, Trip.Environment environment,
        LocalDate from, LocalDate to) {

        User user = userService.getUser();
        LocalDateTime fromDateTime = (from != null) ? from.atStartOfDay() : null;
        LocalDateTime toDateTime = (to != null) ? to.atTime(23, 59, 59) : null;
        Page<Trip> tripPage = tripRepository.search(user,
            (location != null && !location.isBlank()) ? location : null,
            environment, fromDateTime, toDateTime, PageRequest.of(page, size));
        List<TripReturnDto> trips = tripPage.getContent().stream()
            .map(TripService::toReturnDto)
            .collect(Collectors.toList());
        return new TripPageDto(trips, tripPage.hasNext(), tripPage.getTotalElements());
    }

    private Trip findOwnTrip(Long tripId) {
        return tripRepository.findByIdAndUser(tripId, userService.getUser())
            .orElseThrow(() -> new TripNotFoundException(tripId));
    }

    private static SpecialCatchWithIdDto toSpecialCatchDto(Long tripId, SpecialCatch specialCatch) {
        // Checks the id only, so the lazy image itself isn't loaded
        boolean hasImage = specialCatch.getImage() != null;
        return new SpecialCatchWithIdDto(
            specialCatch.getId(),
            specialCatch.getFish().getId(),
            hasImage ? "/api/trips/" + tripId + "/special-catches/" + specialCatch.getId() + "/image" : null,
            specialCatch.getSize(),
            specialCatch.getWeight(),
            specialCatch.getNotes(),
            specialCatch.getFish().getScientificName()
        );
    }

    private static TripReturnDto toReturnDto(Trip trip) {
        return new TripReturnDto(
            trip.getId(),
            trip.getLocation(),
            trip.getEnvironment(),
            trip.getTime(),
            (trip.getDuration() != null) ? trip.getDuration().toHours() : null,
            trip.getTemperature(),
            trip.getWaterLevel(),
            Set.copyOf(trip.getWeather()),
            trip.getNotes()
        );
    }
}
