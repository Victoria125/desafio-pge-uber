package com.vitoria.accountservice.infrastructure.security;

import com.vitoria.accountservice.domain.AuthToken;
import com.vitoria.accountservice.domain.TokenGenerator;
import com.vitoria.accountservice.domain.entities.Account;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtTokenGenerator implements TokenGenerator {
    private final SecretKey secretKey;
    private final long expirationSeconds;

    public JwtTokenGenerator(
            @Value("${security.jwt.secret}") final String secret,
            @Value("${security.jwt.expiration-seconds:86400}") final long expirationSeconds
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationSeconds;
    }

    @Override
    public AuthToken generate(final Account anAccount) {
        final Instant now = Instant.now();
        final String token = Jwts.builder()
                .subject(anAccount.getId())
                .claim("name", anAccount.getName())
                .claim("email", anAccount.getEmail())
                .claim("type", anAccount.getType().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(this.expirationSeconds)))
                .signWith(this.secretKey)
                .compact();
        return new AuthToken(token, this.expirationSeconds);
    }
}
