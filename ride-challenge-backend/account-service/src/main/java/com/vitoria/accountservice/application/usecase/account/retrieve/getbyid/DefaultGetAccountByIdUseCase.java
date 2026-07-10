package com.vitoria.accountservice.application.usecase.account.retrieve.getbyid;

import com.vitoria.accountservice.domain.AccountGateway;

import java.util.NoSuchElementException;
import java.util.Objects;

public class DefaultGetAccountByIdUseCase extends GetAccountByIdUseCase {
    private final AccountGateway accountGateway;

    public DefaultGetAccountByIdUseCase(final AccountGateway accountGateway) {
        this.accountGateway = Objects.requireNonNull(accountGateway);
    }

    @Override
    public AccountOutput execute(final String anId) {
        return this.accountGateway.getById(anId)
                .map(AccountOutput::from)
                .orElseThrow(() -> new NoSuchElementException(
                        "Account with id %s was not found".formatted(anId)));
    }
}
