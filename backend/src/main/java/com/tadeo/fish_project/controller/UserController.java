package com.tadeo.fish_project.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.tadeo.fish_project.dto.UserDto;
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
    public ResponseEntity<String> authenticateAndGetToken(@RequestBody UserDto authRequest) {
        try {
            String token = userService.authenticateAndGetToken(authRequest.username(), authRequest.password());
            return ResponseEntity.ok()
                .body(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Failed to authenticate user: " + authRequest.username() + "\n" + e);
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<UserDetails> getUserDetails() {
        Optional<UserDetails> userDetails = userService.getUserDetails();
        if (userDetails.isPresent()) {
            return ResponseEntity.ok(userDetails.get());
        }
        return ResponseEntity.noContent().build();
    }
}
