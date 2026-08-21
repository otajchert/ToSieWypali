package com.tsw.config;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtUtilTest {

    private static final long EXPIRATION = 60_000;
    private static final String SECRET = encode("0123456789abcdef0123456789abcdef");

    @Test
    void generateAndParseToken() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, EXPIRATION);
        UUID clientId = UUID.randomUUID();

        String token = jwtUtil.generate(clientId);

        assertEquals(clientId.toString(), jwtUtil.parse(token).getSubject());
    }

    private static String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
