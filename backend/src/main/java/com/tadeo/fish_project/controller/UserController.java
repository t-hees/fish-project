package com.tadeo.fish_project.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tadeo.fish_project.dto.PasswordDto;
import com.tadeo.fish_project.dto.UserInfoDto;
import com.tadeo.fish_project.dto.UserPasswordDto;
import com.tadeo.fish_project.service.UserService;
import com.tadeo.fish_project.util.AuthCookies;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/*
The account of the authenticated user
*/
@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserInfoDto> getCurrentUser() {
        return ResponseEntity.ok(new UserInfoDto(userService.getUser().getUsername()));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody UserPasswordDto userPasswordDto) {
        userService.changePassword(userPasswordDto);
        return ResponseEntity.noContent().build();
    }

    // Requires the password again, so a stolen session alone can't delete the account
    @DeleteMapping
    public ResponseEntity<Void> delete(@Valid @RequestBody PasswordDto passwordDto) {
        userService.delete(passwordDto.password());
        return ResponseEntity.noContent()
            .header(HttpHeaders.SET_COOKIE, AuthCookies.expired().toString())
            .build();
    }
}
