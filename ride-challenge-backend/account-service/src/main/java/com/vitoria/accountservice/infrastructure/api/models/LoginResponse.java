package com.vitoria.accountservice.infrastructure.api.models;

import com.vitoria.accountservice.domain.enums.AccountType;

public record LoginResponse(
        String token,
        long expiresIn,
        String accountId,
        String name,
        String email,
        AccountType type
) {
}
