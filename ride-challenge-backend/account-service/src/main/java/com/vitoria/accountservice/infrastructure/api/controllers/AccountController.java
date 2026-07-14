package com.vitoria.accountservice.infrastructure.api.controllers;

import com.vitoria.accountservice.application.usecase.account.create.CreateAccountCommand;
import com.vitoria.accountservice.application.usecase.account.create.CreateAccountOutput;
import com.vitoria.accountservice.application.usecase.account.create.CreateAccountUseCase;
import com.vitoria.accountservice.application.usecase.account.retrieve.getbyid.GetAccountByIdUseCase;
import com.vitoria.accountservice.application.usecase.account.retrieve.list.ListAccountsUseCase;
import com.vitoria.accountservice.infrastructure.api.AccountAPI;
import com.vitoria.accountservice.infrastructure.api.models.AccountResponse;
import com.vitoria.accountservice.infrastructure.api.models.CreateAccountRequest;
import com.vitoria.accountservice.infrastructure.api.models.CreateAccountResponse;
import com.vitoria.accountservice.infrastructure.api.models.ListAccountsResponse;
import com.vitoria.accountservice.infrastructure.api.presenters.AccountPresenter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@RestController
public class AccountController implements AccountAPI {
    private final CreateAccountUseCase createAccountUseCase;
    private final GetAccountByIdUseCase getAccountByIdUseCase;
    private final ListAccountsUseCase listAccountsUseCase;

    public AccountController(
            final CreateAccountUseCase createAccountUseCase,
            final GetAccountByIdUseCase getAccountByIdUseCase,
            final ListAccountsUseCase listAccountsUseCase
    ) {
        this.createAccountUseCase = Objects.requireNonNull(createAccountUseCase);
        this.getAccountByIdUseCase = Objects.requireNonNull(getAccountByIdUseCase);
        this.listAccountsUseCase = Objects.requireNonNull(listAccountsUseCase);
    }

    @Override
    public ResponseEntity<CreateAccountResponse> createAccount(final CreateAccountRequest aRequest) {
        final CreateAccountCommand aCommand =
                new CreateAccountCommand(aRequest.name(), aRequest.email(), aRequest.password(), aRequest.type());
        final CreateAccountOutput anOutput = this.createAccountUseCase.execute(aCommand);
        return ResponseEntity
                .created(URI.create("/accounts/" + anOutput.id()))
                .body(new CreateAccountResponse(anOutput.id()));
    }

    @Override
    public ResponseEntity<AccountResponse> getAccountById(final String id) {
        final AccountResponse response = AccountPresenter.presenterSimple
                .compose(this.getAccountByIdUseCase::execute)
                .apply(id);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<ListAccountsResponse>> listAccounts() {
        final List<ListAccountsResponse> response = this.listAccountsUseCase.execute()
                .stream()
                .map(AccountPresenter.presenterList)
                .toList();
        return ResponseEntity.ok(response);
    }
}
