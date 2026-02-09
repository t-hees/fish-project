package com.tadeo.fish_project;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import com.tadeo.fish_project.config.SecurityConfig;
import com.tadeo.fish_project.config.SecurityNoAuthTestConfig;
import com.tadeo.fish_project.dto.AllCatchesDto;
import com.tadeo.fish_project.dto.EditCatchesDto;
import com.tadeo.fish_project.dto.IdDto;
import com.tadeo.fish_project.dto.SimpleCatchDto;
import com.tadeo.fish_project.dto.SpecialCatchDto;
import com.tadeo.fish_project.dto.TripDto;
import com.tadeo.fish_project.dto.TripReturnDto;
import com.tadeo.fish_project.entity.Trip;
import com.tadeo.fish_project.repository.TripRepository;
import com.tadeo.fish_project.util.TestFishUtils;
import com.tadeo.fish_project.util.TestUserAuth;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class TripIntTest {

    @Autowired
    TestUserAuth testUserAuth;

    @Autowired
    TripRepository tripRepository;

    @Autowired
    TestFishUtils testFishUtils;

    private Long someFishId = null;

    private final TripDto tripDto = new TripDto(
        "cool lake",
        Trip.Environment.LAKE,
        LocalDateTime.of(2026, 04, 05, 6, 30),
        3, 30l, 30000l,
        Set.of(Trip.Weather.CLEAR_SKY, Trip.Weather.LIGHT_RAIN),
        "notes");

    private List<TripReturnDto> getAllTrips() throws RuntimeException {
        return testUserAuth.exchangeRestWithAuth("/api/trip/all", HttpMethod.GET,
            new ParameterizedTypeReference<List<TripReturnDto>>() {}, null,
            HttpStatus.OK, "Failed to get all trips");
    }

    @BeforeEach
    void initTrips() {
        tripRepository.deleteAll();

        someFishId = testFishUtils.initializeTestFish();

        testUserAuth.exchangeRestWithAuth("/api/trip/create", HttpMethod.POST, new ParameterizedTypeReference<String>() {},
            tripDto, HttpStatus.CREATED, "Failed to create trips");
    }

    @Test
    void testDeleteTrip() {
        List<TripReturnDto> trips = getAllTrips();
        assertFalse(trips.isEmpty());
        testUserAuth.exchangeRestWithAuth("/api/trip/delete", HttpMethod.POST,
            new ParameterizedTypeReference<String>() {}, new IdDto(trips.getFirst().id()),
            HttpStatus.OK, "Deletion failed");
        assertEquals(
            (trips.size() > 1) ? trips.subList(1, trips.size()) : new ArrayList<TripReturnDto>(),
            getAllTrips(),
            "Unexpected result after deletion"
        );
    }

    @Test
    void testEditCatches() {
        List<TripReturnDto> trips = getAllTrips();
        assertFalse(trips.isEmpty());
        Long tripId = trips.getFirst().id();

        // Add catches
        EditCatchesDto editCatchesDto = new EditCatchesDto(
            tripId,
            new ArrayList<SimpleCatchDto>() {{
                add(new SimpleCatchDto(someFishId, 4, Optional.of("fish1")));
            }},
            new ArrayList<SpecialCatchDto>() {{
                add(new SpecialCatchDto(someFishId, "data:image/png;base64,3859024=", 24l, 30l,
                    "some notes", "fish2"));
            }},
            new ArrayList<Long>()
        );
        testUserAuth.exchangeRestWithAuth("/api/trip/edit-catches", HttpMethod.POST,
            new ParameterizedTypeReference<String>() {}, editCatchesDto,
            HttpStatus.OK, "Failed adding catches to trip");

        // Get catches
        AllCatchesDto allCatches = testUserAuth.exchangeRestWithAuth("/api/trip/get-catches", HttpMethod.POST,
            new ParameterizedTypeReference<AllCatchesDto>() {}, new IdDto(tripId),
            HttpStatus.OK, "Failed to get all catches");
        assertAll(
            "Getting newly created catches didn't return expected values",
            () -> assertEquals(
                editCatchesDto.simpleCatches(),
                allCatches.simpleCatches().stream()
                    .map(fish -> new SimpleCatchDto(fish.fishId(), fish.amount(), Optional.of("fish1")))
                    .collect(Collectors.toList())
            ),
            () -> assertEquals(
                editCatchesDto.newSpecialCatches(),
                allCatches.specialCatches().stream()
                    .map(spwId -> new SpecialCatchDto(spwId.fishId(), spwId.imageData(),
                        spwId.size(), spwId.weight(), spwId.notes(), "fish2"))
                    .collect(Collectors.toList())
            )
        );

        // Delete catches
        EditCatchesDto deleteCatchesDto = new EditCatchesDto(
            tripId,
            new ArrayList<SimpleCatchDto>(),
            new ArrayList<SpecialCatchDto>(),
            new ArrayList<Long>() {{
                add(allCatches.specialCatches().getFirst().catchId());
            }}
        );
        testUserAuth.exchangeRestWithAuth("/api/trip/edit-catches", HttpMethod.POST,
            new ParameterizedTypeReference<String>() {}, deleteCatchesDto,
            HttpStatus.OK, "Failed deleting catches from trip");

        // Get empty catches
        AllCatchesDto newAllCatches = testUserAuth.exchangeRestWithAuth("/api/trip/get-catches", HttpMethod.POST,
            new ParameterizedTypeReference<AllCatchesDto>() {}, new IdDto(tripId),
            HttpStatus.OK, "Failed to get all catches");
        assertAll(
            "Getting catches after deletion should be empty",
            () -> assertTrue(newAllCatches.simpleCatches().isEmpty()),
            () -> assertTrue(newAllCatches.specialCatches().isEmpty())
        );
    }
}
