package com.vitoria.accountservice.application.usecase.account.retrieve.list;

import com.vitoria.accountservice.domain.AccountGateway;
import com.vitoria.accountservice.domain.entities.Account;
import com.vitoria.accountservice.domain.enums.AccountType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultListAccountsUseCaseTest {
    @InjectMocks
    private DefaultListAccountsUseCase useCase;
    @Mock
    private AccountGateway accountGateway;

    @Test
    void givenAValidQuery_whenCallsListAccounts_thenReturnAccounts() {

        final Account aClient = Account.newAccount("Maria Silva", "maria@email.com", AccountType.CLIENT);
        final Account aDriver = Account.newAccount("Joao Souza", "joao@email.com", AccountType.DRIVER);

        when(this.accountGateway.getAll()).thenReturn(List.of(aClient, aDriver));
        final List<ListAccountsOutput> anOutput = this.useCase.execute();

        assertNotNull(anOutput);
        assertEquals(2, anOutput.size());
        assertEquals(aClient.getId(), anOutput.get(0).id());
        assertEquals(aClient.getName(), anOutput.get(0).name());
        assertEquals(aClient.getEmail(), anOutput.get(0).email());
        assertEquals(aClient.getType(), anOutput.get(0).type());
        assertEquals(aDriver.getId(), anOutput.get(1).id());
        assertEquals(aDriver.getType(), anOutput.get(1).type());
        verify(this.accountGateway, times(1)).getAll();
    }

    @Test
    void givenAValidQuery_whenHasNoAccounts_thenReturnEmptyList() {

        when(this.accountGateway.getAll()).thenReturn(List.of());
        final List<ListAccountsOutput> anOutput = this.useCase.execute();

        assertNotNull(anOutput);
        assertTrue(anOutput.isEmpty());
        verify(this.accountGateway, times(1)).getAll();
    }

    @Test
    void givenAValidQuery_whenGatewayThrows_thenReturnException() {

        final String expectedMessageError = "Gateway Error";

        when(this.accountGateway.getAll()).thenThrow(new IllegalStateException(expectedMessageError));

        final IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> this.useCase.execute());
        assertEquals(expectedMessageError, exception.getMessage());
        verify(this.accountGateway, times(1)).getAll();
    }
}
