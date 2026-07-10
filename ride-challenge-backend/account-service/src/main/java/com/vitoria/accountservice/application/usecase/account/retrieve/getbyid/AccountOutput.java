package com.vitoria.accountservice.application.usecase.account.retrieve.getbyid;

import com.vitoria.accountservice.domain.entities.Account;
import com.vitoria.accountservice.domain.enums.AccountType;

import java.time.Instant;

public record AccountOutput(
        String id,
        String name,
        String email,
        AccountType type,
        Instant createdAt
) {
    public static AccountOutput from(final Account anAccount) {
        return new AccountOutput(
                anAccount.getId(),
                anAccount.getName(),
                anAccount.getEmail(),
                anAccount.getType(),
                anAccount.getCreatedAt()
        );
    }
}
