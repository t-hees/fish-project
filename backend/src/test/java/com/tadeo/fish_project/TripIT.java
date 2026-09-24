package com.tadeo.fish_project;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.tadeo.fish_project.dto.AllCatchesDto;
import com.tadeo.fish_project.dto.EditCatchesDto;
import com.tadeo.fish_project.dto.SimpleCatchDto;
import com.tadeo.fish_project.dto.SpecialCatchDto;
import com.tadeo.fish_project.dto.SpecialCatchWithIdDto;
import com.tadeo.fish_project.dto.TripDto;
import com.tadeo.fish_project.dto.TripPageDto;
import com.tadeo.fish_project.dto.TripReturnDto;
import com.tadeo.fish_project.entity.Trip;
import com.tadeo.fish_project.exception.ApiError;
import com.tadeo.fish_project.util.TestFishUtils;
import com.tadeo.fish_project.util.TestUserAuth;
import com.tadeo.fish_project.util.TestUtils;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TripIT {

    @Autowired
    TestUserAuth testUserAuth;

    @Autowired
    TestUtils testUtils;

    @Autowired
    TestFishUtils testFishUtils;

    private Long someFishId = null;

    private final TripDto tripDto = new TripDto(
        "cool lake",
        Trip.Environment.LAKE,
        LocalDateTime.of(2026, 4, 5, 6, 30),
        3l, 30l, 30000l,
        Set.of(Trip.Weather.CLEAR_SKY, Trip.Weather.LIGHT_RAIN),
        "notes");

    private TripPageDto getTripPage(String queryString) {
        return testUserAuth.exchangeRestWithAuth("/api/trips?" + queryString, HttpMethod.GET,
            new ParameterizedTypeReference<TripPageDto>() {}, null,
            HttpStatus.OK, "Failed to get trips");
    }

    private static String catchesUrl(Long tripId) {
        return "/api/trips/" + tripId + "/catches";
    }

    private List<TripReturnDto> getAllTrips() {
        return getTripPage("page=0&size=50").trips();
    }

    private List<TripReturnDto> searchTrips(String queryString) {
        return getTripPage("page=0&size=50&" + queryString).trips();
    }

    private TripReturnDto createTrip(TripDto dto) {
        return testUserAuth.exchangeRestWithAuth("/api/trips", HttpMethod.POST,
            new ParameterizedTypeReference<TripReturnDto>() {}, dto, HttpStatus.CREATED, "Failed to create trip");
    }

    private TripDto tripAt(String location, LocalDateTime time) {
        return new TripDto(location, Trip.Environment.RIVER, time, null, null, null, Set.of(), null);
    }

    private Long firstTripId() {
        List<TripReturnDto> trips = getAllTrips();
        assertFalse(trips.isEmpty());
        return trips.getFirst().id();
    }

    private AllCatchesDto editCatches(Long tripId, EditCatchesDto dto) {
        return testUserAuth.exchangeRestWithAuth(catchesUrl(tripId), HttpMethod.PUT,
            new ParameterizedTypeReference<AllCatchesDto>() {}, dto, HttpStatus.OK, "Failed editing catches of trip");
    }

    private AllCatchesDto getCatches(Long tripId) {
        return testUserAuth.exchangeRestWithAuth(catchesUrl(tripId), HttpMethod.GET,
            new ParameterizedTypeReference<AllCatchesDto>() {}, null,
            HttpStatus.OK, "Failed to get all catches");
    }

    private ApiError expectEntityNotFound(String username, String url, HttpMethod method, Object data) {
        ApiError error = testUserAuth.exchangeErrorAs(username, url, method, data,
            HttpStatus.NOT_FOUND, "Expected request to be rejected");
        assertEquals("ENTITY_NOT_FOUND", error.code());
        return error;
    }

    private void expectValidationFail(String url, HttpMethod method, Object data) {
        ApiError error = testUserAuth.exchangeErrorAs(TestUserAuth.username, url, method, data,
            HttpStatus.BAD_REQUEST, "Expected invalid request to be rejected");
        assertEquals("VALIDATION_FAILED", error.code());
    }

    @BeforeEach
    void initTrips() {
        testUtils.cleanDatabase();
        someFishId = testFishUtils.initializeTestFish();
        createTrip(tripDto);
    }

    @Test
    void testCreateTrip() {
        TripReturnDto trip = getAllTrips().getFirst();
        assertEquals(
            new TripReturnDto(trip.id(), tripDto.location(), tripDto.environment(), tripDto.time(),
                tripDto.hours(), tripDto.temperature(), tripDto.waterLevel(), tripDto.weather(), tripDto.notes()),
            trip
        );
    }

    @Test
    void testCreateTripReturnsCreatedTrip() {
        ResponseEntity<TripReturnDto> response = testUtils.exchange("/api/trips", HttpMethod.POST,
            new ParameterizedTypeReference<TripReturnDto>() {}, tripAt("created", LocalDateTime.of(2026, 2, 2, 2, 0)),
            testUserAuth.authHeadersFor(TestUserAuth.username));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        TripReturnDto created = response.getBody();
        assertEquals("created", created.location());
        assertEquals(URI.create("/api/trips/" + created.id()), response.getHeaders().getLocation());
    }

    @Test
    void testCreateInvalidTrip() {
        expectValidationFail("/api/trips", HttpMethod.POST, new TripDto(" ", null, LocalDateTime.of(2026, 1, 1, 0, 0),
            null, null, null, null, null));
        expectValidationFail("/api/trips", HttpMethod.POST, new TripDto("no time", null, null,
            null, null, null, null, null));
        expectValidationFail("/api/trips", HttpMethod.POST, new TripDto("negative hours", null,
            LocalDateTime.of(2026, 1, 1, 0, 0), -1l, null, null, null, null));
        assertEquals(1, getAllTrips().size());
    }

    @Test
    void testEditCatchesWithInvalidAmount() {
        expectValidationFail(catchesUrl(firstTripId()), HttpMethod.PUT, new EditCatchesDto(
            List.of(new SimpleCatchDto(someFishId, 0, Optional.empty())), List.of(), List.of()));
    }

    @Test
    void testMalformedRequests() {
        ApiError invalidId = testUserAuth.exchangeErrorAs(TestUserAuth.username, "/api/trips/abc", HttpMethod.DELETE,
            null, HttpStatus.BAD_REQUEST, "Non-numeric trip id should be rejected");
        assertEquals("MALFORMED_REQUEST", invalidId.code());

        ApiError wrongMethod = testUserAuth.exchangeErrorAs(TestUserAuth.username, "/api/trips", HttpMethod.DELETE,
            null, HttpStatus.METHOD_NOT_ALLOWED, "Deleting the trip collection should not be supported");
        assertEquals("METHOD_NOT_ALLOWED", wrongMethod.code());
    }

    @Test
    void testCreateTripWithoutOptionalFields() {
        testUtils.cleanDatabase();
        createTrip(new TripDto("minimal", null, LocalDateTime.of(2026, 1, 1, 0, 0),
            null, null, null, null, null));

        TripReturnDto trip = getAllTrips().getFirst();
        assertAll(
            () -> assertEquals("minimal", trip.location()),
            () -> assertNull(trip.hours()),
            () -> assertNull(trip.notes())
        );
    }

    @Test
    void testDeleteTrip() {
        testUserAuth.exchangeNoContentWithAuth("/api/trips/" + firstTripId(), HttpMethod.DELETE, null,
            "Deletion failed");
        assertTrue(getAllTrips().isEmpty(), "Unexpected result after deletion");
    }

    @Test
    void testDeleteNonexistentTrip() {
        ApiError error = expectEntityNotFound(TestUserAuth.username, "/api/trips/-1", HttpMethod.DELETE, null);
        assertTrue(error.message().contains("Trip"), "Unexpected error message: " + error.message());
    }

    @Test
    void testOtherUserCannotAccessTrip() {
        Long tripId = firstTripId();
        String other = TestUserAuth.otherUsername;

        TripPageDto otherTrips = testUserAuth.exchangeRestAs(other, "/api/trips", HttpMethod.GET,
            new ParameterizedTypeReference<TripPageDto>() {}, null, HttpStatus.OK, "Failed to get trips");
        assertTrue(otherTrips.trips().isEmpty(), "Trips of other users must not be listed");

        expectEntityNotFound(other, catchesUrl(tripId), HttpMethod.GET, null);
        expectEntityNotFound(other, catchesUrl(tripId), HttpMethod.PUT,
            new EditCatchesDto(List.of(new SimpleCatchDto(someFishId, 1, Optional.empty())), List.of(), List.of()));
        expectEntityNotFound(other, "/api/trips/" + tripId, HttpMethod.DELETE, null);

        assertEquals(List.of(tripId), getAllTrips().stream().map(TripReturnDto::id).toList(),
            "Trip of the owner must still exist");
        assertTrue(getCatches(tripId).simpleCatches().isEmpty(), "Catches of the owner must be unchanged");
    }

    @Test
    void testSearchTrips() {
        // @BeforeEach already created "cool lake" (LAKE) on 2026-04-05
        createTrip(new TripDto("River Bend", Trip.Environment.RIVER, LocalDateTime.of(2026, 6, 1, 9, 0),
            2l, 20l, 100l, Set.of(Trip.Weather.CLOUDY), "river notes"));
        createTrip(new TripDto("Ocean Pier", Trip.Environment.OCEAN, LocalDateTime.of(2026, 7, 15, 10, 0),
            4l, 22l, 200l, Set.of(Trip.Weather.CLEAR_SKY), "ocean notes"));

        assertAll(
            "Filtering by location should be a case-insensitive substring match",
            () -> assertEquals(1, searchTrips("location=river").size()),
            () -> assertEquals(1, searchTrips("location=COOL").size()),
            () -> assertEquals(0, searchTrips("location=nonexistent").size())
        );

        assertEquals(1, searchTrips("environment=OCEAN").size(), "Filtering by environment should match exactly");

        assertEquals(2, searchTrips("from=2026-05-01&to=2026-08-01").size(),
            "Date range should include River Bend and Ocean Pier but exclude the earlier lake trip");

        assertEquals(1, searchTrips("location=river&environment=RIVER").size(),
            "Combined filters should apply together");
    }

    @Test
    void testSearchDateRangeIncludesWholeBoundaryDays() {
        createTrip(tripAt("late", LocalDateTime.of(2026, 6, 1, 23, 30)));
        createTrip(tripAt("early", LocalDateTime.of(2026, 6, 1, 0, 0)));

        assertEquals(2, searchTrips("from=2026-06-01&to=2026-06-01").size());
    }

    @Test
    void testPaging() {
        testUtils.cleanDatabase();
        for (int day = 1; day <= 12; day++) {
            createTrip(tripAt("trip " + day, LocalDateTime.of(2026, 1, day, 12, 0)));
        }

        TripPageDto first = getTripPage("page=0&size=5");
        TripPageDto last = getTripPage("page=2&size=5");
        assertAll(
            () -> assertEquals(12, first.totalElements()),
            () -> assertTrue(first.hasNext()),
            () -> assertEquals(List.of("trip 12", "trip 11", "trip 10", "trip 9", "trip 8"),
                first.trips().stream().map(TripReturnDto::location).toList(),
                "Trips should be ordered newest first"),
            () -> assertFalse(last.hasNext()),
            () -> assertEquals(List.of("trip 2", "trip 1"),
                last.trips().stream().map(TripReturnDto::location).toList())
        );
    }

    @Test
    void testPageSizeIsCapped() {
        for (int i = 0; i < 50; i++) {
            createTrip(tripAt("trip " + i, LocalDateTime.of(2025, 1, 1, 0, 0).plusDays(i)));
        }

        TripPageDto page = getTripPage("page=0&size=1000");
        assertAll(
            () -> assertEquals(50, page.trips().size()),
            () -> assertEquals(51, page.totalElements()),
            () -> assertTrue(page.hasNext())
        );
    }

    @Test
    void testEditCatches() {
        Long tripId = firstTripId();

        // Add catches
        SpecialCatchDto specialCatch = new SpecialCatchDto(someFishId, "data:image/png;base64,3859024=", 24l, 30l,
            "some notes", "ignored by backend");
        AllCatchesDto editedCatches = editCatches(tripId, new EditCatchesDto(
            List.of(new SimpleCatchDto(someFishId, 4, Optional.of("ignored by backend"))),
            List.of(specialCatch),
            List.of()));

        // Get catches, names are resolved to the scientific name by the backend
        AllCatchesDto allCatches = getCatches(tripId);
        assertEquals(allCatches, editedCatches, "Editing should return the resulting catches");
        assertEquals(
            List.of(new SimpleCatchDto(someFishId, 4, Optional.of(TestFishUtils.someFishScientificName))),
            allCatches.simpleCatches()
        );
        assertEquals(1, allCatches.specialCatches().size());
        SpecialCatchWithIdDto savedSpecialCatch = allCatches.specialCatches().getFirst();
        assertEquals(
            new SpecialCatchWithIdDto(savedSpecialCatch.catchId(), someFishId, specialCatch.imageData(),
                specialCatch.size(), specialCatch.weight(), specialCatch.notes(), TestFishUtils.someFishScientificName),
            savedSpecialCatch
        );

        // Delete catches
        editCatches(tripId, new EditCatchesDto(List.of(), List.of(), List.of(savedSpecialCatch.catchId())));

        AllCatchesDto newAllCatches = getCatches(tripId);
        assertAll(
            "Getting catches after deletion should be empty",
            () -> assertTrue(newAllCatches.simpleCatches().isEmpty()),
            () -> assertTrue(newAllCatches.specialCatches().isEmpty())
        );
    }

    @Test
    void testEditCatchesReplacesSimpleCatches() {
        Long tripId = firstTripId();

        editCatches(tripId, new EditCatchesDto(
            List.of(new SimpleCatchDto(someFishId, 4, Optional.empty())), List.of(), List.of()));
        editCatches(tripId, new EditCatchesDto(
            List.of(new SimpleCatchDto(someFishId, 2, Optional.empty())), List.of(), List.of()));

        List<SimpleCatchDto> simpleCatches = getCatches(tripId).simpleCatches();
        assertEquals(1, simpleCatches.size(), "Simple catches should be replaced, not appended");
        assertEquals(2, simpleCatches.getFirst().amount());
    }

    @Test
    void testEditCatchesKeepsSpecialCatchesAndAllowsMissingImage() {
        Long tripId = firstTripId();
        SpecialCatchDto withoutImage = new SpecialCatchDto(someFishId, null, null, null, null, null);

        editCatches(tripId, new EditCatchesDto(List.of(), List.of(withoutImage), List.of()));
        editCatches(tripId, new EditCatchesDto(List.of(), List.of(withoutImage), List.of()));

        List<SpecialCatchWithIdDto> specialCatches = getCatches(tripId).specialCatches();
        assertEquals(2, specialCatches.size(), "New special catches should be appended");
        assertTrue(specialCatches.stream().allMatch(c -> c.imageData() == null));
    }

    @Test
    void testEditCatchesWithUnknownFish() {
        Long tripId = firstTripId();
        expectEntityNotFound(TestUserAuth.username, catchesUrl(tripId), HttpMethod.PUT,
            new EditCatchesDto(List.of(new SimpleCatchDto(-1l, 1, Optional.empty())), List.of(), List.of()));
        assertTrue(getCatches(tripId).simpleCatches().isEmpty());
    }

    @Test
    void testGetCatchesOfNonexistentTrip() {
        expectEntityNotFound(TestUserAuth.username, catchesUrl(-1l), HttpMethod.GET, null);
    }
}
