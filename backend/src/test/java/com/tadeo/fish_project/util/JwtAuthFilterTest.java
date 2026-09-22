package com.tadeo.fish_project.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.tadeo.fish_project.exception.UserCredentialsException;
import com.tadeo.fish_project.service.UserService;

import jakarta.servlet.http.Cookie;

class JwtAuthFilterTest {

    private UserService userService;
    private JwtUtil jwtUtil;
    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        jwtUtil = JwtUtilTest.createJwtUtil();
        filter = new JwtAuthFilter(userService, jwtUtil);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private static MockHttpServletRequest requestWithToken(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("AUTH_TOKEN", token));
        return request;
    }

    /*
    Runs the filter and returns the resulting authentication, the chain has to continue in every case
    */
    private Authentication runFilter(MockHttpServletRequest request) throws Exception {
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, new MockHttpServletResponse(), chain);
        assertNotNull(chain.getRequest(), "Filter must always continue the chain");
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Test
    void testValidTokenAuthenticates() throws Exception {
        when(userService.loadUserByUsername("john"))
            .thenReturn(User.withUsername("john").password("pass").build());

        Authentication authentication = runFilter(requestWithToken(jwtUtil.generateToken("john")));
        assertNotNull(authentication);
        assertEquals("john", authentication.getName());
    }

    @Test
    void testRequestWithoutCookieStaysUnauthenticated() throws Exception {
        assertNull(runFilter(new MockHttpServletRequest()));
        verifyNoInteractions(userService);
    }

    @Test
    void testEmptyTokenStaysUnauthenticated() throws Exception {
        assertNull(runFilter(requestWithToken("")));
        verifyNoInteractions(userService);
    }

    @Test
    void testMalformedTokenStaysUnauthenticated() throws Exception {
        assertNull(runFilter(requestWithToken("garbage")));
    }

    @Test
    void testExpiredTokenStaysUnauthenticated() throws Exception {
        String token = JwtUtilTest.signedToken("john", new Date(System.currentTimeMillis() - 1000), JwtUtilTest.secret);
        assertNull(runFilter(requestWithToken(token)));
    }

    @Test
    void testTokenSignedWithOtherKeyStaysUnauthenticated() throws Exception {
        String token = JwtUtilTest.signedToken("john", new Date(System.currentTimeMillis() + 60000),
            JwtUtilTest.otherSecret);
        assertNull(runFilter(requestWithToken(token)));
    }

    @Test
    void testTokenOfDeletedUserStaysUnauthenticated() throws Exception {
        when(userService.loadUserByUsername("ghost")).thenThrow(new UserCredentialsException("Failed to find user"));
        assertNull(runFilter(requestWithToken(jwtUtil.generateToken("ghost"))));
    }
}
