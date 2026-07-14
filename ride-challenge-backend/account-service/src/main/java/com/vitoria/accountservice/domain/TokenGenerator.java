package com.vitoria.accountservice.domain;

import com.vitoria.accountservice.domain.entities.Account;

public interface TokenGenerator {
    AuthToken generate(Account anAccount);
}
