package com.tadeo.fish_project.dto;

public record SpecialCatchDto(Long fishId, byte[] imageData, Long size, Long weight, String notes){};
