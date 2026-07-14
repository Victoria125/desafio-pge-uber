package com.vitoria.accountservice.application.usecase.account.retrieve.getbyid;

import com.vitoria.accountservice.domain.AccountGateway;
import com.vitoria.accountservice.domain.entities.Account;
import com.vitoria.accountservice.domain.enums.AccountType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultGetAccountByIdUseCaseTest {
    @InjectMocks
    private DefaultGetAccountByIdUseCase useCase;
    @Mock
    private AccountGateway accountGateway;

    @Test
    void givenAValidId_whenCallsGetAccountById_thenReturnAccount() {
        
        final Account anAccount =
                Account.newAccount("Joao Motorista", "joao@email.com", "hashed-password", AccountType.DRIVER);
        final String expectedId = anAccount.getId();

        
        when(this.accountGateway.getById(eq(expectedId))).thenReturn(Optional.of(anAccount));
        final AccountOutput anOutput = this.useCase.execute(expectedId);

        
        assertNotNull(anOutput);
        assertEquals(expectedId, anOutput.id());
        assertEquals(anAccount.getName(), anOutput.name());
        assertEquals(anAccount.getEmail(), anOutput.email());
        assertEquals(anAccount.getType(), anOutput.type());
        verify(this.accountGateway, times(1)).getById(eq(expectedId));
    }

    @Test
    void givenAnInvalidId_whenCallsGetAccountById_thenThrowsNotFound() {
        
        final String expectedId = "invalid-id";
        final String expectedMessageError = "Account with id invalid-id was not found";

        
        when(this.accountGateway.getById(eq(expectedId))).thenReturn(Optional.empty());

        
        final NoSuchElementException exception =
                assertThrows(NoSuchElementException.class, () -> this.useCase.execute(expectedId));
        assertEquals(expectedMessageError, exception.getMessage());
        verify(this.accountGateway, times(1)).getById(eq(expectedId));
    }
}
