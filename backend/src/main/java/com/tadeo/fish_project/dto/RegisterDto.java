package com.tadeo.fish_project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/*
Separate from UserDto so the password policy only applies to new passwords, not to logins of existing accounts.
BCrypt only accepts passwords of up to 72 bytes.
*/
public record RegisterDto (
    @NotBlank @Size(max = 50) String username,
    @NotBlank @Size(min = 8, max = 72) String password){};
