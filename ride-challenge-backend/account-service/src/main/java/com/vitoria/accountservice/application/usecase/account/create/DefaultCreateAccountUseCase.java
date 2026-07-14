package com.vitoria.accountservice.application.usecase.account.create;

import com.vitoria.accountservice.domain.AccountGateway;
import com.vitoria.accountservice.domain.PasswordHasher;
import com.vitoria.accountservice.domain.entities.Account;

import java.util.Objects;

public class DefaultCreateAccountUseCase extends CreateAccountUseCase {
    private final AccountGateway accountGateway;
    private final PasswordHasher passwordHasher;

    public DefaultCreateAccountUseCase(
            final AccountGateway accountGateway,
            final PasswordHasher passwordHasher
    ) {
        this.accountGateway = Objects.requireNonNull(accountGateway);
        this.passwordHasher = Objects.requireNonNull(passwordHasher);
    }

    @Override
    public CreateAccountOutput execute(final CreateAccountCommand aCommand) {
        this.accountGateway.getByEmail(aCommand.email())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("'email' is already in use");
                });

        final String aPasswordHash = aCommand.password() == null || aCommand.password().isBlank()
                ? aCommand.password()
                : this.passwordHasher.hash(aCommand.password());

        final Account anAccount = Account.newAccount(
                aCommand.name(),
                aCommand.email(),
                aPasswordHash,
                aCommand.type()
        );
        return new CreateAccountOutput(this.accountGateway.save(anAccount));
    }
}
