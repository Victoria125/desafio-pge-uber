package com.vitoria.accountservice.application.usecase.account.retrieve.list;

import com.vitoria.accountservice.domain.entities.Account;
import com.vitoria.accountservice.domain.enums.AccountType;

public record ListAccountsOutput(
        String id,
        String name,
        String email,
        AccountType type
) {
    public static ListAccountsOutput from(final Account anAccount) {
        return new ListAccountsOutput(
                anAccount.getId(),
                anAccount.getName(),
                anAccount.getEmail(),
                anAccount.getType()
        );
    }
}
