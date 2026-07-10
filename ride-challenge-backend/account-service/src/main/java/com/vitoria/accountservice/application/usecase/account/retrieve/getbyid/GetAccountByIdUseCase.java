package com.vitoria.accountservice.application.usecase.account.retrieve.getbyid;

public abstract class GetAccountByIdUseCase {
    public abstract AccountOutput execute(String anId);
}
