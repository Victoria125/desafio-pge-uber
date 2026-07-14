package com.vitoria.accountservice.domain;

public record AuthToken(
        String token,
        long expiresInSeconds
) {
}
