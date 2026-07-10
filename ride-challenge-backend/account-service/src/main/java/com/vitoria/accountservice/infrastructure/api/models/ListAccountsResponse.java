package com.vitoria.accountservice.infrastructure.api.models;

import com.vitoria.accountservice.domain.enums.AccountType;

public record ListAccountsResponse(
        String id,
        String name,
        String email,
        AccountType type
) {
}
