package com.tadeo.fish_project.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.tadeo.fish_project.dto.SpecialCatchDto;
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
    Encodes a small image in the given ImageIO format (e.g. "png")
    */
    public static byte[] encodedImage(String format) {
        BufferedImage image = new BufferedImage(20, 10, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, format, output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return output.toByteArray();
    }

    /*
    Multipart body for creating a special catch, image may be null for a catch without image
    */
    public static MultiValueMap<String, Object> specialCatchParts(SpecialCatchDto dto, byte[] image) {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        parts.add("catch", new HttpEntity<>(dto, jsonHeaders));
        if (image != null) {
            // Only parts with a file name are received as files
            parts.add("image", new ByteArrayResource(image) {
                @Override
                public String getFilename() {
                    return "photo";
                }
            });
        }
        return parts;
    }

    /*
    Performs a request that is expected to succeed with 204 and returns the response for its headers
    */
    public ResponseEntity<String> exchangeNoContent(String url, HttpMethod method, Object data, HttpHeaders headers,
            String errorMessage) {
        ResponseEntity<String> response = exchange(url, method, new ParameterizedTypeReference<String>() {},
            data, headers);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(), errorMessage + ": " + response.getBody());
        return response;
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
