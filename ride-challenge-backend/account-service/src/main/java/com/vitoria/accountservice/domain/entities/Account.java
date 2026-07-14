package com.vitoria.accountservice.domain.entities;

import com.vitoria.accountservice.domain.enums.AccountType;

import java.time.Instant;
import java.util.UUID;

public class Account {
    private String id;
    private String name;
    private String email;
    private String passwordHash;
    private AccountType type;
    private Instant createdAt;

    public static Account with(
            String id,
            String name,
            String email,
            String passwordHash,
            AccountType type,
            Instant createdAt
    ) {
        return new Account(id, name, email, passwordHash, type, createdAt);
    }

    public static Account newAccount(
            final String aName,
            final String anEmail,
            final String aPasswordHash,
            final AccountType aType
    ) {
        validate(aName, anEmail, aPasswordHash, aType);
        final String anId = UUID.randomUUID().toString();
        final Instant anInstant = Instant.now();
        return new Account(anId, aName, anEmail, aPasswordHash, aType, anInstant);
    }

    private static void validate(
            final String aName,
            final String anEmail,
            final String aPasswordHash,
            final AccountType aType
    ) {
        if (aName == null) {
            throw new IllegalArgumentException("'name' should be not null");
        }

        if (aName.isBlank()) {
            throw new IllegalArgumentException("'name' should be not blank");
        }

        if (anEmail == null) {
            throw new IllegalArgumentException("'email' should be not null");
        }

        if (anEmail.isBlank()) {
            throw new IllegalArgumentException("'email' should be not blank");
        }

        if (aPasswordHash == null) {
            throw new IllegalArgumentException("'password' should be not null");
        }

        if (aPasswordHash.isBlank()) {
            throw new IllegalArgumentException("'password' should be not blank");
        }

        if (aType == null) {
            throw new IllegalArgumentException("'type' should be not null");
        }
    }

    private Account(
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

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public AccountType getType() {
        return type;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
