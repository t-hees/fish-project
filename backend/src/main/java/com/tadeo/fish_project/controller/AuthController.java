package com.tadeo.fish_project.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tadeo.fish_project.dto.RegisterDto;
import com.tadeo.fish_project.dto.UserDto;
import com.tadeo.fish_project.dto.UserInfoDto;
import com.tadeo.fish_project.service.UserService;
import com.tadeo.fish_project.util.AuthCookies;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserInfoDto> register(@Valid @RequestBody RegisterDto data) {
        userService.createUser(data.username(), data.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserInfoDto(data.username()));
    }

    @PostMapping("/login")
    public ResponseEntity<UserInfoDto> login(@Valid @RequestBody UserDto authRequest) {
        String token = userService.login(authRequest.username(), authRequest.password());
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, AuthCookies.withToken(token).toString())
            .body(new UserInfoDto(authRequest.username()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
            .header(HttpHeaders.SET_COOKIE, AuthCookies.expired().toString())
            .build();
    }
}
