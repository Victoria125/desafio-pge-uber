package com.vitoria.accountservice.infrastructure.security;

import com.vitoria.accountservice.domain.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class BCryptPasswordHasher implements PasswordHasher {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hash(final String aRawPassword) {
        return this.encoder.encode(aRawPassword);
    }

    @Override
    public boolean matches(final String aRawPassword, final String aPasswordHash) {
        return this.encoder.matches(aRawPassword, aPasswordHash);
    }
}
