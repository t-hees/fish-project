package com.tadeo.fish_project;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.tadeo.fish_project.config.SecurityConfig;
import com.tadeo.fish_project.dto.StringDto;
import com.tadeo.fish_project.dto.UserDto;
import com.tadeo.fish_project.dto.UserPasswordDto;
import com.tadeo.fish_project.repository.UserRepository;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class UserIntTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    UserRepository userRepository;

    private final UserDto userDto = new UserDto("john", "strongpass");

    private ResponseEntity<String> performLogin(UserDto userDto) {
        ResponseEntity<String> response = restTemplate.exchange("/api/user/login",
            HttpMethod.POST,
            new HttpEntity<>(userDto, new HttpHeaders()),
            String.class);
        return response;
    }

    private HttpHeaders performLoginAndGetHeaders(UserDto userDto) {
        ResponseEntity<String> response = performLogin(userDto);
        assertTrue(HttpStatus.OK.equals(response.getStatusCode()));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.COOKIE, response.getHeaders().getFirst(HttpHeaders.SET_COOKIE));
        return httpHeaders;
    }

    private ResponseEntity<String> performRegister(UserDto userDto) {
        return restTemplate.exchange("/api/user/register",
            HttpMethod.POST,
            new HttpEntity<>(userDto, new HttpHeaders()),
            String.class);
    }

    @BeforeEach
    void initUser() {
        userRepository.deleteAll();
        ResponseEntity<String> response = performRegister(userDto);
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
    void testLogin() {
        assertTrue(userRepository.count() != 0);
        // fail login with false password
        UserDto falseLogin = new UserDto(userDto.username(), userDto.password() + "f");
        ResponseEntity<String> response = performLogin(falseLogin);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), response.getBody());

        // succesfull login
        response = performLogin(userDto);
        assertEquals(HttpStatus.OK, response.getStatusCode(), response.getBody());
        assertTrue(response.hasBody(), "Login didn't return anything");
    }

    @Test
    void testGetUsername() {
        HttpHeaders httpHeaders = performLoginAndGetHeaders(userDto);

        ResponseEntity<String> response = restTemplate.exchange("/api/user/name",
            HttpMethod.GET,
            new HttpEntity<>(null, httpHeaders),
            String.class);
        assertEquals(userDto.username(), response.getBody());
    }

    @Test
    void testChangePasswordWithRelogin() {
        HttpHeaders httpHeaders = performLoginAndGetHeaders(userDto);

        String newPass = "newpass";

        // Change password
        UserPasswordDto userPasswordDto = new UserPasswordDto(userDto.password()+"fail", newPass);
        ResponseEntity<String> response = restTemplate.exchange("/api/user/change-password",
            HttpMethod.POST,
            new HttpEntity<>(userPasswordDto, httpHeaders),
            String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), response.getBody());
        userPasswordDto = new UserPasswordDto(userDto.password(), newPass);
        response = restTemplate.exchange("/api/user/change-password",
            HttpMethod.POST,
            new HttpEntity<>(userPasswordDto, httpHeaders),
            String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode(), response.getBody());

        // Logout
        response = restTemplate.exchange("/api/user/logout",
            HttpMethod.POST,
            new HttpEntity<>(null, httpHeaders),
            String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Failed to logout");
        httpHeaders.clear();
        httpHeaders.add(HttpHeaders.COOKIE, response.getHeaders().getFirst(HttpHeaders.SET_COOKIE));
        response = restTemplate.exchange("/api/user/name",
            HttpMethod.GET,
            new HttpEntity<>(null, httpHeaders),
            String.class);
        assertEquals(
            HttpStatus.FORBIDDEN,
            response.getStatusCode(),
            "Non-authorized access should be forbidden after logout"
        );

        // Login again
        response = performLogin(userDto);
        assertEquals(
            HttpStatus.BAD_REQUEST, response.getStatusCode(),
            "Login with old password is expected to fail" + response.getBody()
        );
        response = performLogin(new UserDto(userDto.username(), newPass));
        assertEquals(
            HttpStatus.OK, response.getStatusCode(),
            "Login with new password failed" + response.getBody()
        );
    }

    @Test
    void testDelete() {
        HttpHeaders httpHeaders = performLoginAndGetHeaders(userDto);

        ResponseEntity<String> response = restTemplate.exchange("/api/user/delete",
            HttpMethod.POST,
            new HttpEntity<>(new StringDto(userDto.password()+"fail"), httpHeaders),
            String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Expected to fail user deletion with false password");
        response = restTemplate.exchange("/api/user/delete",
            HttpMethod.POST,
            new HttpEntity<>(new StringDto(userDto.password()), httpHeaders),
            String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Failed user deletion" + response.getBody());
    }
}
