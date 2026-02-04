package com.tadeo.fish_project.controller;

import java.time.Duration;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tadeo.fish_project.dto.StringDto;
import com.tadeo.fish_project.dto.UserDto;
import com.tadeo.fish_project.dto.UserPasswordDto;
import com.tadeo.fish_project.entity.User;
import com.tadeo.fish_project.service.UserService;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> createUser(@RequestBody UserDto data) {
        try {
            User user = userService.createUser(data.username(), data.password());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Created user: " + data.username());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to create user: " + data.username() + "\n" + e);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserDto authRequest) {
        try {
            String token = userService.login(authRequest.username(), authRequest.password());

            ResponseCookie cookie = ResponseCookie.from("AUTH_TOKEN", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(Duration.ofHours(24))
                .build();

            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(authRequest.username());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Failed to authenticate user: " + authRequest.username() + "\n" + e);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        ResponseCookie expiredCookie = ResponseCookie.from("AUTH_TOKEN", "")
            .path("/")
            .httpOnly(true)
            .secure(true)
            .maxAge(0)
            .sameSite("Strict")
            .build();

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
            .body("Logged out");
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody UserPasswordDto userPasswordDto) {
        try {
            userService.changePassword(userPasswordDto);
            return ResponseEntity.ok("Successfully changed password");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Failed to change password:\n" + e);
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<String> delete(@RequestBody StringDto passwordDto) {
        try {
            userService.delete(passwordDto.string());
            return ResponseEntity.ok("Successfully deleted user");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Failed to delete user:\n" + e);
        }
    }

    @GetMapping("/name")
    public ResponseEntity<String> getUsername() {
        Optional<String> userDetails = userService.getUsername();
        if (userDetails.isPresent()) {
            return ResponseEntity.ok(userDetails.get());
        }
        return ResponseEntity.noContent().build();
    }
}
