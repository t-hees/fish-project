package com.tadeo.fish_project.util;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import com.tadeo.fish_project.exception.ApiError;
import com.tadeo.fish_project.repository.UserRepository;
import com.tadeo.fish_project.service.UserService;

@Component
@Profile("test")
public class TestUserAuth {
    public static final String username = "john";
    public static final String otherUsername = "jane";
    public static final String password = "strongpass";

    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    TestUtils testUtils;

    public static HttpHeaders cookieHeaders(String authToken) {
        ResponseCookie cookie = ResponseCookie.from("AUTH_TOKEN", authToken)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .sameSite("Strict")
            .maxAge(Duration.ofHours(24))
            .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, cookie.toString());
        return headers;
    }

    /*
    Creates the user if missing and returns headers carrying a valid auth cookie for it
    */
    public HttpHeaders authHeadersFor(String name) {
        if (!userRepository.findByUsername(name).isPresent()) {
            userService.createUser(name, password);
        }
        return cookieHeaders(jwtUtil.generateToken(name));
    }

    public <RetType> RetType exchangeRestAs(String name, String url, HttpMethod method,
            ParameterizedTypeReference<RetType> typeReference, Object data, HttpStatus expectedStatus,
            String errorMessage) {
        return testUtils.exchangeRest(url, method, typeReference, data, authHeadersFor(name),
            expectedStatus, errorMessage);
    }

    public <RetType> RetType exchangeRestWithAuth(String url, HttpMethod method,
            ParameterizedTypeReference<RetType> typeReference, Object data, HttpStatus expectedStatus,
            String errorMessage) {
        return exchangeRestAs(username, url, method, typeReference, data, expectedStatus, errorMessage);
    }

    public void exchangeNoContentWithAuth(String url, HttpMethod method, Object data, String errorMessage) {
        testUtils.exchangeNoContent(url, method, data, authHeadersFor(username), errorMessage);
    }

    public ApiError exchangeErrorAs(String name, String url, HttpMethod method, Object data,
            HttpStatus expectedStatus, String errorMessage) {
        return testUtils.exchangeError(url, method, data, authHeadersFor(name), expectedStatus, errorMessage);
    }
}
