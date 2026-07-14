package com.vitoria.accountservice.application.usecase.auth;

public abstract class AuthenticateUseCase {
    public abstract AuthenticateOutput execute(AuthenticateCommand aCommand);
}
