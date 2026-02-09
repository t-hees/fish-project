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

import com.tadeo.fish_project.repository.UserRepository;
import com.tadeo.fish_project.service.UserService;

@Component
@Profile("test")
public class TestUserAuth {
    public static final String username = "john";
    public static final String password = "strongpass";

    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    TestUtils testUtils;

    private HttpHeaders authHeaders = null;

    private void createTestUser() {
        userService.createUser(username, password);
        String authToken = jwtUtil.generateToken(username);
        ResponseCookie cookie = ResponseCookie.from("AUTH_TOKEN", authToken)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .sameSite("Strict")
            .maxAge(Duration.ofHours(24))
            .build();

        authHeaders = new HttpHeaders();
        authHeaders.add(HttpHeaders.COOKIE, cookie.toString());
    }

    public <RetType> RetType exchangeRestWithAuth(String url, HttpMethod method,
            ParameterizedTypeReference<RetType> typeReference, Object data, HttpStatus expectedStatus,
            String errorMessage) {
        if ((!userRepository.findByUsername(username).isPresent()) || authHeaders == null) {
            createTestUser();
        }
        return testUtils.exchangeRest(url, method, typeReference, data, authHeaders, expectedStatus, errorMessage);
    }
}
