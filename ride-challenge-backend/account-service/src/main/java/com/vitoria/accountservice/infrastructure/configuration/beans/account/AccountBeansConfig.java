package com.vitoria.accountservice.infrastructure.configuration.beans.account;

import com.vitoria.accountservice.application.usecase.account.create.CreateAccountUseCase;
import com.vitoria.accountservice.application.usecase.account.create.DefaultCreateAccountUseCase;
import com.vitoria.accountservice.application.usecase.account.retrieve.getbyid.DefaultGetAccountByIdUseCase;
import com.vitoria.accountservice.application.usecase.account.retrieve.getbyid.GetAccountByIdUseCase;
import com.vitoria.accountservice.application.usecase.account.retrieve.list.DefaultListAccountsUseCase;
import com.vitoria.accountservice.application.usecase.account.retrieve.list.ListAccountsUseCase;
import com.vitoria.accountservice.application.usecase.auth.AuthenticateUseCase;
import com.vitoria.accountservice.application.usecase.auth.DefaultAuthenticateUseCase;
import com.vitoria.accountservice.domain.AccountGateway;
import com.vitoria.accountservice.domain.PasswordHasher;
import com.vitoria.accountservice.domain.TokenGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
public class AccountBeansConfig {
    private final AccountGateway accountGateway;
    private final PasswordHasher passwordHasher;
    private final TokenGenerator tokenGenerator;

    public AccountBeansConfig(
            final AccountGateway accountGateway,
            final PasswordHasher passwordHasher,
            final TokenGenerator tokenGenerator
    ) {
        this.accountGateway = Objects.requireNonNull(accountGateway);
        this.passwordHasher = Objects.requireNonNull(passwordHasher);
        this.tokenGenerator = Objects.requireNonNull(tokenGenerator);
    }

    @Bean
    public CreateAccountUseCase createAccountUseCase() {
        return new DefaultCreateAccountUseCase(accountGateway, passwordHasher);
    }

    @Bean
    public GetAccountByIdUseCase getAccountByIdUseCase() {
        return new DefaultGetAccountByIdUseCase(accountGateway);
    }

    @Bean
    public ListAccountsUseCase listAccountsUseCase() {
        return new DefaultListAccountsUseCase(accountGateway);
    }

    @Bean
    public AuthenticateUseCase authenticateUseCase() {
        return new DefaultAuthenticateUseCase(accountGateway, passwordHasher, tokenGenerator);
    }
}
