package com.vitoria.accountservice.infrastructure.api;

import com.vitoria.accountservice.infrastructure.api.models.AccountResponse;
import com.vitoria.accountservice.infrastructure.api.models.CreateAccountRequest;
import com.vitoria.accountservice.infrastructure.api.models.CreateAccountResponse;
import com.vitoria.accountservice.infrastructure.api.models.ListAccountsResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(value = "/accounts")
public interface AccountAPI {

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CreateAccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest aRequest);

    @GetMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<AccountResponse> getAccountById(@PathVariable(name = "id") String id);

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<ListAccountsResponse>> listAccounts();
}
