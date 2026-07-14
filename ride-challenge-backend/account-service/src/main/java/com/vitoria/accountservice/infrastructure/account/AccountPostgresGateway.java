package com.vitoria.accountservice.infrastructure.account;

import com.vitoria.accountservice.domain.AccountGateway;
import com.vitoria.accountservice.domain.entities.Account;
import com.vitoria.accountservice.infrastructure.account.persistence.AccountEntity;
import com.vitoria.accountservice.infrastructure.account.persistence.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AccountPostgresGateway implements AccountGateway {
    private final AccountRepository accountRepository;

    public AccountPostgresGateway(final AccountRepository accountRepository) {
        this.accountRepository = Objects.requireNonNull(accountRepository);
    }

    @Override
    public String save(final Account anAccount) {
        final AccountEntity anEntity = AccountEntity.from(anAccount);
        return this.accountRepository.save(anEntity).getId();
    }

    @Override
    public Optional<Account> getById(final String anId) {
        return this.accountRepository.findById(anId)
                .map(AccountEntity::toAggregate);
    }

    @Override
    public Optional<Account> getByEmail(final String anEmail) {
        return this.accountRepository.findByEmail(anEmail)
                .map(AccountEntity::toAggregate);
    }

    @Override
    public List<Account> getAll() {
        return this.accountRepository
                .findAll()
                .stream()
                .map(AccountEntity::toAggregate)
                .toList();
    }
}
