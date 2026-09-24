package com.tadeo.fish_project.dto;

import jakarta.validation.constraints.NotBlank;

public record StringDto (@NotBlank String string){};
