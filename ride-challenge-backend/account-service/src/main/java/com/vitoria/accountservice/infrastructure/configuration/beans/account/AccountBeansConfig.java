package com.vitoria.accountservice.infrastructure.configuration.beans.account;

import com.vitoria.accountservice.application.usecase.account.create.CreateAccountUseCase;
import com.vitoria.accountservice.application.usecase.account.create.DefaultCreateAccountUseCase;
import com.vitoria.accountservice.application.usecase.account.retrieve.getbyid.DefaultGetAccountByIdUseCase;
import com.vitoria.accountservice.application.usecase.account.retrieve.getbyid.GetAccountByIdUseCase;
import com.vitoria.accountservice.application.usecase.account.retrieve.list.DefaultListAccountsUseCase;
import com.vitoria.accountservice.application.usecase.account.retrieve.list.ListAccountsUseCase;
import com.vitoria.accountservice.domain.AccountGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
public class AccountBeansConfig {
    private final AccountGateway accountGateway;

    public AccountBeansConfig(final AccountGateway accountGateway) {
        this.accountGateway = Objects.requireNonNull(accountGateway);
    }

    @Bean
    public CreateAccountUseCase createAccountUseCase() {
        return new DefaultCreateAccountUseCase(accountGateway);
    }

    @Bean
    public GetAccountByIdUseCase getAccountByIdUseCase() {
        return new DefaultGetAccountByIdUseCase(accountGateway);
    }

    @Bean
    public ListAccountsUseCase listAccountsUseCase() {
        return new DefaultListAccountsUseCase(accountGateway);
    }
}
