package com.vitoria.accountservice.application.usecase.auth;

public record AuthenticateCommand(
        String email,
        String password
) {
}
