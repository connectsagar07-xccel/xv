package com.logicleaf.invplatform.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final String SECRET = "your-very-secret-and-long-key-change-this";
    private static final long ACCESS_TOKEN_VALIDITY = 1000 * 60 * 15; // 15 min
    private static final long REFRESH_TOKEN_VALIDITY = 1000 * 60 * 60 * 24; // 24 hrs

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generateAccessToken(String username) {
        return generateToken(username, ACCESS_TOKEN_VALIDITY);
    }

    public String generateRefreshToken(String username) {
        return generateToken(username, REFRESH_TOKEN_VALIDITY);
    }

    private String generateToken(String subject, long validity) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validity);

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
