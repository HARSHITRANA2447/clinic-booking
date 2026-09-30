package com.clinicbooking.clinicbackend.security;

import com.clinicbooking.clinicbackend.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expiryMinutes;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiry-minutes}") long expiryMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiryMinutes = expiryMinutes;
    }

    public long expirySeconds() {
        return Duration.ofMinutes(expiryMinutes).toSeconds();
    }

    public String generate(User u) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(u.getId()))
                .claim("role", u.getRole().name())
                .claim("pid", u.getPatientId())
                .claim("cid", u.getClinicId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(Duration.ofMinutes(expiryMinutes))))
                .signWith(key)
                .compact();
    }

    /** Returns empty if the token is invalid, tampered with, or expired. */
    public Optional<AuthUser> parse(String token) {
        try {
            Claims c = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload();
            return Optional.of(new AuthUser(
                    Long.valueOf(c.getSubject()),
                    asLong(c.get("pid")),
                    asLong(c.get("cid")),
                    User.Role.valueOf(c.get("role", String.class))));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static Long asLong(Object o) {
        return o == null ? null : ((Number) o).longValue();
    }
}