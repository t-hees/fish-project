package com.tadeo.fish_project.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordDto (@NotBlank String password){};
