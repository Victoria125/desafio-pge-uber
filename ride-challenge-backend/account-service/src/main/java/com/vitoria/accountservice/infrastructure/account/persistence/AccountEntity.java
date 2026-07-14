package com.vitoria.accountservice.infrastructure.account.persistence;

import com.vitoria.accountservice.domain.entities.Account;
import com.vitoria.accountservice.domain.enums.AccountType;
import jakarta.persistence.*;

import java.time.Instant;

@Table(name = "tb_accounts")
@Entity
public class AccountEntity {
    @Id
    @Column(name = "account_id", nullable = false)
    private String id;

    @Column(name = "account_name", nullable = false)
    private String name;

    @Column(name = "account_email", nullable = false, unique = true)
    private String email;

    @Column(name = "account_password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType type;

    @Column(name = "account_created_at", nullable = false)
    private Instant createdAt;

    public static AccountEntity from(final Account anAccount) {
        return new AccountEntity(
                anAccount.getId(),
                anAccount.getName(),
                anAccount.getEmail(),
                anAccount.getPasswordHash(),
                anAccount.getType(),
                anAccount.getCreatedAt()
        );
    }

    public static Account toAggregate(final AccountEntity anEntity) {
        return Account.with(
                anEntity.getId(),
                anEntity.getName(),
                anEntity.getEmail(),
                anEntity.getPasswordHash(),
                anEntity.getType(),
                anEntity.getCreatedAt()
        );
    }

    private AccountEntity(
            final String id,
            final String name,
            final String email,
            final String passwordHash,
            final AccountType type,
            final Instant createdAt
    ) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.type = type;
        this.createdAt = createdAt;
    }

    public AccountEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
