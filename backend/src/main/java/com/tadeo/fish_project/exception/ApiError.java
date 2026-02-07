package com.tadeo.fish_project.exception;

import java.time.Instant;

public record ApiError(String code, String message, Instant timestamp, String uri){};
