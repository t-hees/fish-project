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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.tadeo.fish_project.dto.AllCatchesDto;
import com.tadeo.fish_project.dto.EditCatchesDto;
import com.tadeo.fish_project.dto.PasswordDto;
import com.tadeo.fish_project.dto.SimpleCatchDto;
import com.tadeo.fish_project.dto.SpecialCatchDto;
import com.tadeo.fish_project.dto.TripDto;
import com.tadeo.fish_project.dto.TripReturnDto;
import com.tadeo.fish_project.dto.UserDto;
import com.tadeo.fish_project.dto.UserInfoDto;
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

    @Autowired
    JdbcTemplate jdbcTemplate;

    private static final String currentUserUrl = "/api/users/me";

    private final UserDto userDto = new UserDto("john", "strongpass");

    private ResponseEntity<String> exchange(String url, HttpMethod method, Object data, HttpHeaders headers) {
        return testUtils.exchange(url, method, new ParameterizedTypeReference<String>() {}, data, headers);
    }

    private HttpStatus getCurrentUserStatus(HttpHeaders headers) {
        return HttpStatus.valueOf(exchange(currentUserUrl, HttpMethod.GET, null, headers).getStatusCode().value());
    }

    private ResponseEntity<UserInfoDto> performLogin(UserDto userDto) {
        return testUtils.exchange("/api/auth/login", HttpMethod.POST,
            new ParameterizedTypeReference<UserInfoDto>() {}, userDto, new HttpHeaders());
    }

    /*
    Logs in through the API and returns headers carrying the returned auth cookie
    */
    private HttpHeaders performLoginAndGetHeaders(UserDto userDto) {
        ResponseEntity<UserInfoDto> response = performLogin(userDto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return cookieHeadersOf(response);
    }

    private static HttpHeaders cookieHeadersOf(ResponseEntity<?> response) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.COOKIE, response.getHeaders().getFirst(HttpHeaders.SET_COOKIE));
        return httpHeaders;
    }

    private ApiError expectAuthFail(String url, HttpMethod method, Object data, HttpHeaders headers,
            String errorMessage) {
        ApiError error = testUtils.exchangeError(url, method, data, headers,
            HttpStatus.UNAUTHORIZED, errorMessage);
        assertEquals("AUTH_FAIL", error.code());
        return error;
    }

    private void expectValidationFail(String url, Object data, String errorMessage) {
        ApiError error = testUtils.exchangeError(url, HttpMethod.POST, data, new HttpHeaders(),
            HttpStatus.BAD_REQUEST, errorMessage);
        assertEquals("VALIDATION_FAILED", error.code());
    }

    @BeforeEach
    void initUser() {
        testUtils.cleanDatabase();
        UserInfoDto created = testUtils.exchangeRest("/api/auth/register", HttpMethod.POST,
            new ParameterizedTypeReference<UserInfoDto>() {}, userDto, HttpStatus.CREATED, "Failed to register");
        assertEquals(userDto.username(), created.username());
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
        ApiError error = testUtils.exchangeError("/api/auth/register", HttpMethod.POST, userDto, new HttpHeaders(),
            HttpStatus.CONFLICT, "Duplicate username should be rejected");
        assertEquals("USERNAME_TAKEN", error.code());
        assertEquals(1, userRepository.count());
    }

    @Test
    void testCreateUserWithInvalidCredentials() {
        expectValidationFail("/api/auth/register", new UserDto(null, "longenough"),
            "Missing username should be rejected");
        expectValidationFail("/api/auth/register", new UserDto("name", null),
            "Missing password should be rejected");
        expectValidationFail("/api/auth/register", new UserDto("name", "short"),
            "Too short password should be rejected");
        assertEquals(1, userRepository.count());
    }

    @Test
    void testLogin() {
        ApiError wrongPassword = expectAuthFail("/api/auth/login", HttpMethod.POST,
            new UserDto(userDto.username(), userDto.password() + "f"),
            new HttpHeaders(), "Login with wrong password should fail");
        ApiError unknownUser = expectAuthFail("/api/auth/login", HttpMethod.POST,
            new UserDto("unknown", userDto.password()), new HttpHeaders(), "Login of unknown user should fail");
        assertEquals(wrongPassword.message(), unknownUser.message(),
            "Login errors shouldn't reveal whether a username exists");

        ResponseEntity<UserInfoDto> response = performLogin(userDto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userDto.username(), response.getBody().username());
        String cookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertTrue(cookie != null && cookie.startsWith("AUTH_TOKEN="), "Login didn't set auth cookie");
        assertTrue(cookie.contains("HttpOnly"), "Auth cookie must be HttpOnly");
    }

    @Test
    void testGetCurrentUser() {
        UserInfoDto user = testUtils.exchangeRest(currentUserUrl, HttpMethod.GET,
            new ParameterizedTypeReference<UserInfoDto>() {}, null, performLoginAndGetHeaders(userDto),
            HttpStatus.OK, "Failed to get current user");
        assertEquals(userDto.username(), user.username());
    }

    @Test
    void testProtectedEndpointsRequireAuth() {
        assertEquals(HttpStatus.UNAUTHORIZED, getCurrentUserStatus(new HttpHeaders()));
        assertEquals(HttpStatus.UNAUTHORIZED,
            exchange("/api/trips", HttpMethod.GET, null, new HttpHeaders()).getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED,
            exchange("/api/fish?name=aal", HttpMethod.GET, null, new HttpHeaders()).getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"garbage", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.invalidsignature"})
    void testInvalidTokenIsRejected(String token) {
        assertEquals(HttpStatus.UNAUTHORIZED, getCurrentUserStatus(TestUserAuth.cookieHeaders(token)));
    }

    @Test
    void testTamperedTokenIsRejected() {
        String[] parts = jwtUtil.generateToken(userDto.username()).split("\\.");
        // Keep header and signature of a valid token but claim to be another user
        String forgedPayload = java.util.Base64.getUrlEncoder().withoutPadding()
            .encodeToString("{\"sub\":\"admin\"}".getBytes());
        String forged = parts[0] + "." + forgedPayload + "." + parts[2];
        assertEquals(HttpStatus.UNAUTHORIZED, getCurrentUserStatus(TestUserAuth.cookieHeaders(forged)));
    }

    @Test
    void testTokenOfDeletedUserIsRejected() {
        String token = jwtUtil.generateToken("ghost");
        assertEquals(HttpStatus.UNAUTHORIZED, getCurrentUserStatus(TestUserAuth.cookieHeaders(token)));
    }

    @Test
    void testLogoutWorksWithoutValidToken() {
        ResponseEntity<String> response = testUtils.exchangeNoContent("/api/auth/logout", HttpMethod.POST, null,
            TestUserAuth.cookieHeaders("garbage"), "Logout should always clear the cookie");
        assertTrue(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE).contains("Max-Age=0"));
    }

    @Test
    void testChangePasswordWithRelogin() {
        HttpHeaders httpHeaders = performLoginAndGetHeaders(userDto);
        String newPass = "newpassword";
        String passwordUrl = currentUserUrl + "/password";

        // Change password
        expectAuthFail(passwordUrl, HttpMethod.PUT, new UserPasswordDto(userDto.password() + "fail", newPass),
            httpHeaders, "Changing password with wrong old password should fail");
        testUtils.exchangeNoContent(passwordUrl, HttpMethod.PUT, new UserPasswordDto(userDto.password(), newPass),
            httpHeaders, "Failed to change password");

        // Logout
        ResponseEntity<String> response = testUtils.exchangeNoContent("/api/auth/logout", HttpMethod.POST, null,
            httpHeaders, "Failed to logout");
        assertEquals(
            HttpStatus.UNAUTHORIZED,
            getCurrentUserStatus(cookieHeadersOf(response)),
            "Non-authorized access should be rejected after logout"
        );

        // Login again
        expectAuthFail("/api/auth/login", HttpMethod.POST, userDto, new HttpHeaders(),
            "Login with old password is expected to fail");
        performLoginAndGetHeaders(new UserDto(userDto.username(), newPass));
    }

    @Test
    void testDelete() {
        HttpHeaders httpHeaders = performLoginAndGetHeaders(userDto);

        expectAuthFail(currentUserUrl, HttpMethod.DELETE, new PasswordDto(userDto.password() + "fail"), httpHeaders,
            "Expected to fail user deletion with false password");
        ResponseEntity<String> response = testUtils.exchangeNoContent(currentUserUrl, HttpMethod.DELETE,
            new PasswordDto(userDto.password()), httpHeaders, "Failed user deletion");
        assertFalse(userRepository.findByUsername(userDto.username()).isPresent());
        assertTrue(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE).contains("Max-Age=0"),
            "Deleting the account should clear the auth cookie");
    }

    @Test
    void testDeleteUserWithTrips() {
        Long fishId = testFishUtils.initializeTestFish();
        Long tripId = testUserAuth.exchangeRestWithAuth("/api/trips", HttpMethod.POST,
            new ParameterizedTypeReference<TripReturnDto>() {},
            new TripDto("lake", Trip.Environment.LAKE, LocalDateTime.of(2026, 4, 5, 6, 30),
                null, null, null, Set.of(), null),
            HttpStatus.CREATED, "Failed to create trip").id();
        testUserAuth.exchangeRestWithAuth("/api/trips/" + tripId + "/catches", HttpMethod.PUT,
            new ParameterizedTypeReference<AllCatchesDto>() {},
            new EditCatchesDto(List.of(new SimpleCatchDto(fishId, 1, Optional.empty())), List.of()),
            HttpStatus.OK, "Failed to add catches");
        ResponseEntity<String> specialCatch = testUserAuth.postMultipartAs(TestUserAuth.username,
            "/api/trips/" + tripId + "/special-catches",
            TestUtils.specialCatchParts(new SpecialCatchDto(fishId, 1l, 1l, null), TestUtils.encodedImage("png")),
            new ParameterizedTypeReference<String>() {});
        assertEquals(HttpStatus.CREATED, specialCatch.getStatusCode(), specialCatch.getBody());

        testUtils.exchangeNoContent(currentUserUrl, HttpMethod.DELETE, new PasswordDto(TestUserAuth.password),
            testUserAuth.authHeadersFor(TestUserAuth.username), "Failed deleting user with trips");
        assertEquals(0, tripRepository.count(), "Trips of deleted user should be deleted as well");
        assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM image", Long.class),
            "Images of deleted user should be deleted as well");
    }
}
