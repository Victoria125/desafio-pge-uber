package com.vitoria.accountservice.application.usecase.auth;

import com.vitoria.accountservice.domain.AuthToken;
import com.vitoria.accountservice.domain.entities.Account;
import com.vitoria.accountservice.domain.enums.AccountType;

public record AuthenticateOutput(
        String token,
        long expiresIn,
        String accountId,
        String name,
        String email,
        AccountType type
) {
    public static AuthenticateOutput from(final Account anAccount, final AuthToken aToken) {
        return new AuthenticateOutput(
                aToken.token(),
                aToken.expiresInSeconds(),
                anAccount.getId(),
                anAccount.getName(),
                anAccount.getEmail(),
                anAccount.getType()
        );
    }
}
