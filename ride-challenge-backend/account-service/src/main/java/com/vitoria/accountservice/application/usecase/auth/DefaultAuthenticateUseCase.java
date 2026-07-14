package com.vitoria.accountservice.application.usecase.auth;

import com.vitoria.accountservice.domain.AccountGateway;
import com.vitoria.accountservice.domain.AuthToken;
import com.vitoria.accountservice.domain.PasswordHasher;
import com.vitoria.accountservice.domain.TokenGenerator;
import com.vitoria.accountservice.domain.entities.Account;
import com.vitoria.accountservice.domain.exceptions.InvalidCredentialsException;

import java.util.Objects;

public class DefaultAuthenticateUseCase extends AuthenticateUseCase {
    private final AccountGateway accountGateway;
    private final PasswordHasher passwordHasher;
    private final TokenGenerator tokenGenerator;

    public DefaultAuthenticateUseCase(
            final AccountGateway accountGateway,
            final PasswordHasher passwordHasher,
            final TokenGenerator tokenGenerator
    ) {
        this.accountGateway = Objects.requireNonNull(accountGateway);
        this.passwordHasher = Objects.requireNonNull(passwordHasher);
        this.tokenGenerator = Objects.requireNonNull(tokenGenerator);
    }

    @Override
    public AuthenticateOutput execute(final AuthenticateCommand aCommand) {
        final Account anAccount = this.accountGateway.getByEmail(aCommand.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!this.passwordHasher.matches(aCommand.password(), anAccount.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        final AuthToken aToken = this.tokenGenerator.generate(anAccount);
        return AuthenticateOutput.from(anAccount, aToken);
    }
}
