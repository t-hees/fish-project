package com.tadeo.fish_project.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.tadeo.fish_project.exception.ApiError;
import com.tadeo.fish_project.repository.FishRepository;
import com.tadeo.fish_project.repository.TripRepository;
import com.tadeo.fish_project.repository.UserRepository;

@Component
@Profile("test")
public class TestUtils {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    TripRepository tripRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FishRepository fishRepository;

    /*
    Deletes all rows in an order that respects foreign keys (trips cascade to their catches)
    so every test class starts from an empty database regardless of execution order
    */
    public void cleanDatabase() {
        tripRepository.deleteAll();
        userRepository.deleteAll();
        fishRepository.deleteAll();
    }

    public <RetType> ResponseEntity<RetType> exchange(String url, HttpMethod method,
            ParameterizedTypeReference<RetType> typeReference, Object data, HttpHeaders headers) {
        return restTemplate.exchange(url, method, new HttpEntity<>(data, headers), typeReference);
    }

    public <RetType> RetType exchangeRest(String url, HttpMethod method,
            ParameterizedTypeReference<RetType> typeReference, Object data, HttpHeaders headers,
            HttpStatus expectedStatus, String errorMessage) {
        ResponseEntity<RetType> response = exchange(url, method, typeReference, data, headers);
        assertEquals(expectedStatus, response.getStatusCode(), errorMessage + ": " + response.getBody());
        assertTrue(response.hasBody(), "Response body is empty");
        return response.getBody();
    }

    public <RetType> RetType exchangeRest(String url, HttpMethod method,
            ParameterizedTypeReference<RetType> typeReference, Object data, HttpStatus expectedStatus,
            String errorMessage) {
        return exchangeRest(url, method, typeReference, data, new HttpHeaders(), expectedStatus, errorMessage);
    }

    public <RetType> RetType exchangeRest(String url, HttpMethod method,
            ParameterizedTypeReference<RetType> typeReference, HttpStatus expectedStatus, String errorMessage) {
        return exchangeRest(url, method, typeReference, null, expectedStatus, errorMessage);
    }

    /*
    Performs a request that is expected to be rejected by GlobalExceptionHandler and returns the error body
    */
    public ApiError exchangeError(String url, HttpMethod method, Object data, HttpHeaders headers,
            HttpStatus expectedStatus, String errorMessage) {
        return exchangeRest(url, method, new ParameterizedTypeReference<ApiError>() {}, data, headers,
            expectedStatus, errorMessage);
    }
}
