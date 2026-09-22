package com.tadeo.fish_project.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

class JwtUtilTest {
    static final String secret = "pTj+DBXRkyetocsvSTE1n4r95Gq8rCnCeKvl2qDNBDc=";
    static final String otherSecret = "YW5vdGhlci1zZWNyZXQta2V5LWZvci11bml0LXRlc3Q=";

    private JwtUtil jwtUtil;

    static JwtUtil createJwtUtil() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
        return jwtUtil;
    }

    static String signedToken(String subject, Date expiration, String signingSecret) {
        return Jwts.builder()
            .subject(subject)
            .expiration(expiration)
            .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(signingSecret)))
            .compact();
    }

    private static UserDetails user(String username) {
        return User.withUsername(username).password("pass").build();
    }

    @BeforeEach
    void setUp() {
        jwtUtil = createJwtUtil();
    }

    @Test
    void testGeneratedTokenRoundTrip() {
        String token = jwtUtil.generateToken("john");
        assertEquals("john", jwtUtil.extractUsername(token));
        assertTrue(jwtUtil.validateToken(token, user("john")));
    }

    @Test
    void testTokenOfOtherUserIsInvalid() {
        assertFalse(jwtUtil.validateToken(jwtUtil.generateToken("john"), user("jane")));
    }

    @Test
    void testExpiredTokenIsRejected() {
        String token = signedToken("john", new Date(System.currentTimeMillis() - 1000), secret);
        assertThrows(ExpiredJwtException.class, () -> jwtUtil.validateToken(token, user("john")));
    }

    @Test
    void testTokenSignedWithOtherKeyIsRejected() {
        String token = signedToken("john", new Date(System.currentTimeMillis() + 60000), otherSecret);
        assertThrows(SignatureException.class, () -> jwtUtil.extractUsername(token));
    }

    @Test
    void testMalformedTokenIsRejected() {
        assertThrows(MalformedJwtException.class, () -> jwtUtil.extractUsername("garbage"));
    }
}
