package com.appweb.application.utils;

import com.appweb.application.config.ApplicationConfig;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private SecretKey signingKey;
    private static final long EXPIRATION_TIME = 1000L * 60 * 60 * 24 * 30 * 6; // 6 months

    public static int getExpirationDays () {
        return (int) (EXPIRATION_TIME / 1000 / 60 / 60 / 24);
    }

    private final ApplicationConfig applicationConfig;

    @PostConstruct
    void initSigningKey() {

        if (applicationConfig.getJwtSecretKey() == null || applicationConfig.getJwtSecretKey().isBlank()) {
            throw new IllegalStateException("JWT secret key is not configured (appweb.lowerappname.auth.jwt_secret_key)");
        }
        if (applicationConfig.getJwtSecretKey().getBytes().length < 32) {
            throw new IllegalStateException("JWT secret key must be at least 32 bytes");
        }
        this.signingKey = Keys.hmacShaKeyFor(applicationConfig.getJwtSecretKey().getBytes());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token)
                .getBody().getSubject();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        final Date expiration = Jwts.parserBuilder().setSigningKey(signingKey).build()
                .parseClaimsJws(token).getBody().getExpiration();
        return expiration.before(new Date());
    }
}
