package com.tadeo.fish_project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.tadeo.fish_project.dto.EditCatchesDto;
import com.tadeo.fish_project.dto.SimpleCatchDto;
import com.tadeo.fish_project.dto.SpecialCatchDto;
import com.tadeo.fish_project.dto.StringDto;
import com.tadeo.fish_project.dto.TripDto;
import com.tadeo.fish_project.dto.TripPageDto;
import com.tadeo.fish_project.dto.UserDto;
import com.tadeo.fish_project.dto.UserPasswordDto;
import com.tadeo.fish_project.entity.Trip;
import com.tadeo.fish_project.exception.ApiError;
import com.tadeo.fish_project.repository.TripRepository;
import com.tadeo.fish_project.repository.UserRepository;
import com.tadeo.fish_project.util.JwtUtil;
import com.tadeo.fish_project.util.TestFishUtils;
import com.tadeo.fish_project.util.TestUserAuth;
import com.tadeo.fish_project.util.TestUtils;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserIT {

    @Autowired
    TestUtils testUtils;

    @Autowired
    TestUserAuth testUserAuth;

    @Autowired
    TestFishUtils testFishUtils;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TripRepository tripRepository;

    @Autowired
    JwtUtil jwtUtil;

    private final UserDto userDto = new UserDto("john", "strongpass");

    private ResponseEntity<String> post(String url, Object data, HttpHeaders headers) {
        return testUtils.exchange(url, HttpMethod.POST, new ParameterizedTypeReference<String>() {}, data, headers);
    }

    private ResponseEntity<String> getUsername(HttpHeaders headers) {
        return testUtils.exchange("/api/user/name", HttpMethod.GET,
            new ParameterizedTypeReference<String>() {}, null, headers);
    }

    private ResponseEntity<String> performLogin(UserDto userDto) {
        return post("/api/user/login", userDto, new HttpHeaders());
    }

    /*
    Logs in through the API and returns headers carrying the returned auth cookie
    */
    private HttpHeaders performLoginAndGetHeaders(UserDto userDto) {
        ResponseEntity<String> response = performLogin(userDto);
        assertEquals(HttpStatus.OK, response.getStatusCode(), response.getBody());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.COOKIE, response.getHeaders().getFirst(HttpHeaders.SET_COOKIE));
        return httpHeaders;
    }

    private void expectAuthFail(String url, Object data, HttpHeaders headers, String errorMessage) {
        ApiError error = testUtils.exchangeError(url, HttpMethod.POST, data, headers,
            HttpStatus.BAD_REQUEST, errorMessage);
        assertEquals("AUTH_FAIL", error.code());
    }

    @BeforeEach
    void initUser() {
        testUtils.cleanDatabase();
        ResponseEntity<String> response = post("/api/user/register", userDto, new HttpHeaders());
        assertEquals(HttpStatus.CREATED, response.getStatusCode(), response.getBody());
    }

    @Test
    void testCreateUser() {
        assertTrue(
            userRepository.findByUsername(userDto.username()).isPresent(),
            "User did not actually save in repo"
        );
    }

    @Test
    void testCreateDuplicateUser() {
        expectAuthFail("/api/user/register", userDto, new HttpHeaders(), "Duplicate username should be rejected");
        assertEquals(1, userRepository.count());
    }

    @Test
    void testCreateUserWithoutCredentials() {
        expectAuthFail("/api/user/register", new UserDto(null, "pass"), new HttpHeaders(),
            "Missing username should be rejected");
        expectAuthFail("/api/user/register", new UserDto("name", null), new HttpHeaders(),
            "Missing password should be rejected");
    }

    @Test
    void testLogin() {
        expectAuthFail("/api/user/login", new UserDto(userDto.username(), userDto.password() + "f"),
            new HttpHeaders(), "Login with wrong password should fail");
        expectAuthFail("/api/user/login", new UserDto("unknown", userDto.password()),
            new HttpHeaders(), "Login of unknown user should fail");

        ResponseEntity<String> response = performLogin(userDto);
        assertEquals(HttpStatus.OK, response.getStatusCode(), response.getBody());
        String cookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertTrue(cookie != null && cookie.startsWith("AUTH_TOKEN="), "Login didn't set auth cookie");
        assertTrue(cookie.contains("HttpOnly"), "Auth cookie must be HttpOnly");
    }

    @Test
    void testGetUsername() {
        ResponseEntity<String> response = getUsername(performLoginAndGetHeaders(userDto));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userDto.username(), response.getBody());
    }

    @Test
    void testProtectedEndpointsRequireAuth() {
        assertEquals(HttpStatus.FORBIDDEN, getUsername(new HttpHeaders()).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, testUtils.exchange("/api/trip/all", HttpMethod.GET,
            new ParameterizedTypeReference<String>() {}, null, new HttpHeaders()).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, testUtils.exchange("/api/fish/search_by_common_name?name=aal",
            HttpMethod.GET, new ParameterizedTypeReference<String>() {}, null, new HttpHeaders()).getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"garbage", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.invalidsignature"})
    void testInvalidTokenIsRejected(String token) {
        assertEquals(HttpStatus.FORBIDDEN, getUsername(TestUserAuth.cookieHeaders(token)).getStatusCode());
    }

    @Test
    void testTamperedTokenIsRejected() {
        String[] parts = jwtUtil.generateToken(userDto.username()).split("\\.");
        // Keep header and signature of a valid token but claim to be another user
        String forgedPayload = java.util.Base64.getUrlEncoder().withoutPadding()
            .encodeToString("{\"sub\":\"admin\"}".getBytes());
        String forged = parts[0] + "." + forgedPayload + "." + parts[2];
        assertEquals(HttpStatus.FORBIDDEN, getUsername(TestUserAuth.cookieHeaders(forged)).getStatusCode());
    }

    @Test
    void testTokenOfDeletedUserIsRejected() {
        String token = jwtUtil.generateToken("ghost");
        assertEquals(HttpStatus.FORBIDDEN, getUsername(TestUserAuth.cookieHeaders(token)).getStatusCode());
    }

    @Test
    void testChangePasswordWithRelogin() {
        HttpHeaders httpHeaders = performLoginAndGetHeaders(userDto);
        String newPass = "newpass";

        // Change password
        expectAuthFail("/api/user/change-password", new UserPasswordDto(userDto.password() + "fail", newPass),
            httpHeaders, "Changing password with wrong old password should fail");
        ResponseEntity<String> response = post("/api/user/change-password",
            new UserPasswordDto(userDto.password(), newPass), httpHeaders);
        assertEquals(HttpStatus.OK, response.getStatusCode(), response.getBody());

        // Logout
        response = post("/api/user/logout", null, httpHeaders);
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Failed to logout");
        HttpHeaders loggedOutHeaders = new HttpHeaders();
        loggedOutHeaders.add(HttpHeaders.COOKIE, response.getHeaders().getFirst(HttpHeaders.SET_COOKIE));
        assertEquals(
            HttpStatus.FORBIDDEN,
            getUsername(loggedOutHeaders).getStatusCode(),
            "Non-authorized access should be forbidden after logout"
        );

        // Login again
        expectAuthFail("/api/user/login", userDto, new HttpHeaders(), "Login with old password is expected to fail");
        performLoginAndGetHeaders(new UserDto(userDto.username(), newPass));
    }

    @Test
    void testDelete() {
        HttpHeaders httpHeaders = performLoginAndGetHeaders(userDto);

        expectAuthFail("/api/user/delete", new StringDto(userDto.password() + "fail"), httpHeaders,
            "Expected to fail user deletion with false password");
        ResponseEntity<String> response = post("/api/user/delete", new StringDto(userDto.password()), httpHeaders);
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Failed user deletion" + response.getBody());
        assertFalse(userRepository.findByUsername(userDto.username()).isPresent());
    }

    @Test
    void testDeleteUserWithTrips() {
        Long fishId = testFishUtils.initializeTestFish();
        testUserAuth.exchangeRestWithAuth("/api/trip/create", HttpMethod.POST,
            new ParameterizedTypeReference<String>() {},
            new TripDto("lake", Trip.Environment.LAKE, LocalDateTime.of(2026, 4, 5, 6, 30),
                null, null, null, Set.of(), null),
            HttpStatus.CREATED, "Failed to create trip");
        Long tripId = testUserAuth.exchangeRestWithAuth("/api/trip/all", HttpMethod.GET,
            new ParameterizedTypeReference<TripPageDto>() {}, null, HttpStatus.OK, "Failed to get trips")
            .trips().getFirst().id();
        testUserAuth.exchangeRestWithAuth("/api/trip/edit-catches", HttpMethod.POST,
            new ParameterizedTypeReference<String>() {},
            new EditCatchesDto(tripId, List.of(new SimpleCatchDto(fishId, 1, Optional.empty())),
                List.of(new SpecialCatchDto(fishId, "data:image/png;base64,AAAA", 1l, 1l, null, null)), List.of()),
            HttpStatus.OK, "Failed to add catches");

        ResponseEntity<String> response = post("/api/user/delete", new StringDto(TestUserAuth.password),
            testUserAuth.authHeadersFor(TestUserAuth.username));
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Failed deleting user with trips: " + response.getBody());
        assertEquals(0, tripRepository.count(), "Trips of deleted user should be deleted as well");
    }
}
