package com.vitoria.accountservice.infrastructure.api.presenters;

import com.vitoria.accountservice.application.usecase.account.retrieve.getbyid.AccountOutput;
import com.vitoria.accountservice.application.usecase.account.retrieve.list.ListAccountsOutput;
import com.vitoria.accountservice.infrastructure.api.models.AccountResponse;
import com.vitoria.accountservice.infrastructure.api.models.ListAccountsResponse;

import java.util.function.Function;

public interface AccountPresenter {
    Function<AccountOutput, AccountResponse> presenterSimple = output -> new AccountResponse(
            output.id(),
            output.name(),
            output.email(),
            output.type(),
            output.createdAt()
    );

    Function<ListAccountsOutput, ListAccountsResponse> presenterList = output -> new ListAccountsResponse(
            output.id(),
            output.name(),
            output.email(),
            output.type()
    );
}
