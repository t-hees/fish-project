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

@Component
@Profile("test")
public class TestUtils {

    @Autowired
    TestRestTemplate restTemplate;

    public <RetType> RetType exchangeRest(String url, HttpMethod method,
            ParameterizedTypeReference<RetType> typeReference, Object data, HttpHeaders headers,
            HttpStatus expectedStatus, String errorMessage) {
        ResponseEntity<RetType> response = restTemplate.exchange(url,
                method,
                new HttpEntity<>(data, headers),
                typeReference);
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
}
