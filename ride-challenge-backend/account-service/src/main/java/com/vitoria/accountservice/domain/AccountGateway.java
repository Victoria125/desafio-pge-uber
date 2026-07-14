package com.vitoria.accountservice.domain;

import com.vitoria.accountservice.domain.entities.Account;

import java.util.List;
import java.util.Optional;

public interface AccountGateway {
    String save(Account anAccount);

    Optional<Account> getById(String anId);

    Optional<Account> getByEmail(String anEmail);

    List<Account> getAll();
}
