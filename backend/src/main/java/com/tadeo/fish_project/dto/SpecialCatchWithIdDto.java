package com.tadeo.fish_project.dto;

/*
imageUrl is null for catches without an image
*/
public record SpecialCatchWithIdDto(
    Long catchId, Long fishId, String imageUrl,
    Long size, Long weight, String notes, String name) {};
