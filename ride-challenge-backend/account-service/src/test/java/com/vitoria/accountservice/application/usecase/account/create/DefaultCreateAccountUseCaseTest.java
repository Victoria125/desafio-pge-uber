package com.vitoria.accountservice.application.usecase.account.create;

import com.vitoria.accountservice.domain.AccountGateway;
import com.vitoria.accountservice.domain.enums.AccountType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultCreateAccountUseCaseTest {
    @InjectMocks
    private DefaultCreateAccountUseCase useCase;
    @Mock
    private AccountGateway accountGateway;

    @Test
    void givenAValidParams_whenCallsCreateAccount_thenReturnAccountId() {
        
        final String expectedId = UUID.randomUUID().toString();
        final CreateAccountCommand aCommand =
                new CreateAccountCommand("Maria Silva", "maria@email.com", AccountType.CLIENT);

        
        when(this.accountGateway.save(any())).thenReturn(expectedId);
        final CreateAccountOutput anOutput = this.useCase.execute(aCommand);

        
        assertNotNull(anOutput);
        assertEquals(expectedId, anOutput.id());
        verify(this.accountGateway, times(1)).save(any());
    }

    @Test
    void givenAnInvalidNullName_whenCallsCreateAccount_thenThrowsException() {
        
        final String expectedMessageError = "'name' should be not null";
        final CreateAccountCommand aCommand =
                new CreateAccountCommand(null, "maria@email.com", AccountType.CLIENT);

        
        final IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> this.useCase.execute(aCommand));

        
        assertEquals(expectedMessageError, exception.getMessage());
        verify(this.accountGateway, times(0)).save(any());
    }

    @Test
    void givenAnInvalidBlankName_whenCallsCreateAccount_thenThrowsException() {
        
        final String expectedMessageError = "'name' should be not blank";
        final CreateAccountCommand aCommand =
                new CreateAccountCommand("", "maria@email.com", AccountType.CLIENT);

        
        final IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> this.useCase.execute(aCommand));

        
        assertEquals(expectedMessageError, exception.getMessage());
        verify(this.accountGateway, times(0)).save(any());
    }

    @Test
    void givenAValidParams_whenGatewayThrows_thenReturnException() {
        
        final String expectedMessageError = "Gateway Error";
        final CreateAccountCommand aCommand =
                new CreateAccountCommand("Maria Silva", "maria@email.com", AccountType.DRIVER);

        
        when(this.accountGateway.save(any())).thenThrow(new IllegalStateException(expectedMessageError));

        
        final IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> this.useCase.execute(aCommand));
        assertEquals(expectedMessageError, exception.getMessage());
        verify(this.accountGateway, times(1)).save(any());
    }
}
