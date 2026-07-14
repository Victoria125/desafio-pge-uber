package com.vitoria.accountservice.domain;

public interface PasswordHasher {
    String hash(String aRawPassword);

    boolean matches(String aRawPassword, String aPasswordHash);
}
