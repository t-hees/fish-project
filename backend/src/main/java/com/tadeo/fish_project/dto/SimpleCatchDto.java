package com.tadeo.fish_project.dto;

import java.util.Optional;

public record SimpleCatchDto(Long fishId, Integer amount, Optional<String> name){};
