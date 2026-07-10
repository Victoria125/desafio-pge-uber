package com.vitoria.accountservice.application.usecase.account.retrieve.list;

import com.vitoria.accountservice.domain.AccountGateway;

import java.util.List;
import java.util.Objects;

public class DefaultListAccountsUseCase extends ListAccountsUseCase {
    private final AccountGateway accountGateway;

    public DefaultListAccountsUseCase(final AccountGateway accountGateway) {
        this.accountGateway = Objects.requireNonNull(accountGateway);
    }

    @Override
    public List<ListAccountsOutput> execute() {
        return this.accountGateway.getAll()
                .stream()
                .map(ListAccountsOutput::from)
                .toList();
    }
}
