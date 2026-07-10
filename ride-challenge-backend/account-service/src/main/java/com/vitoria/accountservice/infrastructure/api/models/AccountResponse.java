package com.vitoria.accountservice.infrastructure.api.models;

import com.vitoria.accountservice.domain.enums.AccountType;

import java.time.Instant;

public record AccountResponse(
        String id,
        String name,
        String email,
        AccountType type,
        Instant createdAt
) {
}
