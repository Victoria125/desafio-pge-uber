package com.vitoria.accountservice.application.usecase.account.create;

public abstract class CreateAccountUseCase {
    public abstract CreateAccountOutput execute(CreateAccountCommand aCommand);
}
