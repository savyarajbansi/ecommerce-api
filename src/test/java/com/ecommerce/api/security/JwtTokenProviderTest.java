package com.ecommerce.api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(
                "test-secret-key-at-least-32-characters-long!!",
                86400000L
        );
    }

    @Test
    void generateToken_andExtractEmail() {
        String token = provider.generateToken("user@test.com");
        assertNotNull(token);
        assertEquals("user@test.com", provider.getEmailFromToken(token));
    }

    @Test
    void validateToken_validToken_returnsTrue() {
        String token = provider.generateToken("user@test.com");
        assertTrue(provider.validateToken(token));
    }

    @Test
    void validateToken_invalidToken_returnsFalse() {
        assertFalse(provider.validateToken("not.a.valid.token"));
    }
}
