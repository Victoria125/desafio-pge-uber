package com.vitoria.accountservice.application.usecase.account.retrieve.list;

import java.util.List;

public abstract class ListAccountsUseCase {
    public abstract List<ListAccountsOutput> execute();
}
