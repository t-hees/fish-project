package com.tadeo.fish_project;

import java.time.LocalDateTime;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import com.tadeo.fish_project.dto.IdDto;
import com.tadeo.fish_project.dto.TripDto;
import com.tadeo.fish_project.entity.Trip;
import com.tadeo.fish_project.repository.TripRepository;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WithMockUser(username = "testuser")
@ActiveProfiles("test")
class TripIntTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    TripRepository tripRepository;

    /*
    private final TripDto tripDto = new TripDto(
        "cool lake",
        Trip.Environment.LAKE,
        LocalDateTime.of(2026, 04, 05, 6, 30),
        3, 30l, 30000l,
        Set.of(Trip.Weather.CLEAR_SKY, Trip.Weather.LIGHT_RAIN),
        "notes");

    private List<TripReturnDto> getAllTrips() throws Exception {
        ResponseEntity<?> response = restTemplate.exchange("/api/trip/all",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Object>() {});
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException((String) response.getBody());
        }
        if (response.getBody() instanceof List && response.getBody()) {
            return (List<TripReturnDto>) response.getBody();
        }
    }

    @BeforeEach
    void initTrips() {
        tripRepository.deleteAll();

        ResponseEntity<String> response = restTemplate.exchange("/api/trip/create",
            HttpMethod.POST,
            new HttpEntity<>(tripDto, new HttpHeaders()),
            String.class);
    }

    @Test
    void testDeleteTrip() {
        ResponseEntity<?> response = getAllTrips();
        ResponseEntity<String> response = restTemplate.exchange("/api/trip/delete",
            HttpMethod.POST,
            new HttpEntity<>(new IdDto(), new HttpHeaders()),
            String.class);

    }
    */
}
