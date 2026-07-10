package com.vitoria.accountservice.application.usecase.account.create;

import com.vitoria.accountservice.domain.enums.AccountType;

public record CreateAccountCommand(
        String name,
        String email,
        AccountType type
) {
}
