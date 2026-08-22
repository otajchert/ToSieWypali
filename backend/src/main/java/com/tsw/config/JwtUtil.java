package com.tsw.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private static final int KEY_LENGTH_BYTES = 32;
    private static final String SIGNATURE_ALGORITHM = "HS256";

    private final SecretKey key;
    private final long expiration;

    public JwtUtil(@Value("${jwt.secret}") String encodedSecret,
                   @Value("${jwt.expiration}") long expiration) {
        if (encodedSecret == null || encodedSecret.isBlank()) {
            throw new IllegalArgumentException("JWT secret must not be empty");
        }
        if (expiration <= 0) {
            throw new IllegalArgumentException("JWT expiration must be positive");
        }

        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(encodedSecret);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("JWT secret must be Base64 encoded", exception);
        }

        if (keyBytes.length != KEY_LENGTH_BYTES) {
            throw new IllegalArgumentException("JWT secret must contain exactly 32 bytes");
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expiration = expiration;
    }

    public String generate(UUID clientId) {
        return Jwts.builder()
                .subject(clientId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public Claims parse(String token) {
        Jws<Claims> signedClaims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);

        if (!SIGNATURE_ALGORITHM.equals(signedClaims.getHeader().getAlgorithm())) {
            throw new UnsupportedJwtException("Unsupported JWT signing algorithm");
        }

        return signedClaims.getPayload();
    }

}
