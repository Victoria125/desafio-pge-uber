package com.vitoria.accountservice.application.usecase.account.create;

import com.vitoria.accountservice.domain.AccountGateway;
import com.vitoria.accountservice.domain.entities.Account;

import java.util.Objects;

public class DefaultCreateAccountUseCase extends CreateAccountUseCase {
    private final AccountGateway accountGateway;

    public DefaultCreateAccountUseCase(final AccountGateway accountGateway) {
        this.accountGateway = Objects.requireNonNull(accountGateway);
    }

    @Override
    public CreateAccountOutput execute(final CreateAccountCommand aCommand) {
        final Account anAccount = Account.newAccount(aCommand.name(), aCommand.email(), aCommand.type());
        return new CreateAccountOutput(this.accountGateway.save(anAccount));
    }
}
