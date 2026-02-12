package com.firomsa.inventory.v1.service;

import com.firomsa.inventory.config.AuthSecret;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JWTAuthService {

    private final int JWT_DURATION = 15;
    private final AuthSecret authSecret;
    private final Random random = new Random();

    public String generateToken(String subject) {
        String tokenId = String.valueOf(random.nextInt(10000));
        var now = new Date(System.currentTimeMillis());

        return Jwts.builder().header().keyId(tokenId).and().subject(subject).issuedAt(now)
                .expiration(new Date(now.getTime() + TimeUnit.MINUTES.toMillis(JWT_DURATION)))
                .signWith(Keys.hmacShaKeyFor(authSecret.getSecret().getBytes())).compact();
    }

    public Claims getClaims(String token) {
        return Jwts.parser().verifyWith(Keys.hmacShaKeyFor(authSecret.getSecret().getBytes()))
                .build().parseSignedClaims(token).getPayload();
    }

    public boolean isValidToken(String token) {
        return getExpirationDate(token).after(new Date(System.currentTimeMillis()));
    }

    public boolean isValidToken(String token, String username) {
        String tokenUserName = getSubject(token);

        return (username.equals(tokenUserName) && !isTokenExpired(token));
    }

    public boolean isTokenExpired(String token) {
        return getExpirationDate(token).before(new Date(System.currentTimeMillis()));
    }

    public Date getExpirationDate(String token) {
        return getClaims(token).getExpiration();
    }

    public String getSubject(String token) {
        return getClaims(token).getSubject();
    }
}
