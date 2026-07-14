package com.vitoria.accountservice.infrastructure.account;

import com.vitoria.accountservice.domain.AccountGateway;
import com.vitoria.accountservice.domain.entities.Account;
import com.vitoria.accountservice.domain.enums.AccountType;
import com.vitoria.accountservice.infrastructure.account.persistence.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import(AccountPostgresGateway.class)
class AccountPostgresGatewayIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private AccountGateway accountGateway;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void cleanUp() {
        this.accountRepository.deleteAll();
    }

    @Test
    void givenAValidAccount_whenCallsSave_thenPersistsOnPostgres() {

        final Account anAccount = Account.newAccount("Maria Silva", "maria@email.com", AccountType.CLIENT);

        final String anId = this.accountGateway.save(anAccount);

        assertEquals(anAccount.getId(), anId);
        assertEquals(1, this.accountRepository.count());
    }

    @Test
    void givenAPersistedAccount_whenCallsGetById_thenReturnsAccount() {

        final Account anAccount = Account.newAccount("Joao Souza", "joao@email.com", AccountType.DRIVER);
        this.accountGateway.save(anAccount);

        final Optional<Account> actual = this.accountGateway.getById(anAccount.getId());

        assertTrue(actual.isPresent());
        assertEquals(anAccount.getId(), actual.get().getId());
        assertEquals(anAccount.getName(), actual.get().getName());
        assertEquals(anAccount.getEmail(), actual.get().getEmail());
        assertEquals(anAccount.getType(), actual.get().getType());
    }

    @Test
    void givenAnUnknownId_whenCallsGetById_thenReturnsEmpty() {

        final Optional<Account> actual = this.accountGateway.getById("unknown-id");

        assertTrue(actual.isEmpty());
    }

    @Test
    void givenPersistedAccounts_whenCallsGetAll_thenReturnsAll() {

        this.accountGateway.save(Account.newAccount("Maria Silva", "maria@email.com", AccountType.CLIENT));
        this.accountGateway.save(Account.newAccount("Joao Souza", "joao@email.com", AccountType.DRIVER));

        final List<Account> actual = this.accountGateway.getAll();

        assertEquals(2, actual.size());
    }
}
