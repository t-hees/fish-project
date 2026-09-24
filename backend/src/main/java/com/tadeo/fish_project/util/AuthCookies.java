package com.tadeo.fish_project.util;

import java.time.Duration;

import org.springframework.http.ResponseCookie;

/*
The auth token is only ever sent as a HttpOnly cookie, so scripts in the browser can't read it
*/
public final class AuthCookies {
    public static final String name = "AUTH_TOKEN";

    private AuthCookies() {}

    public static ResponseCookie withToken(String token) {
        return build(token, Duration.ofHours(24));
    }

    public static ResponseCookie expired() {
        return build("", Duration.ZERO);
    }

    private static ResponseCookie build(String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .sameSite("Strict")
            .maxAge(maxAge)
            .build();
    }
}
