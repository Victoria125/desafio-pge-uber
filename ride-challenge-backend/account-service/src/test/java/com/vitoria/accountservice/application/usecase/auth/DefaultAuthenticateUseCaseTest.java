package com.vitoria.accountservice.application.usecase.auth;

import com.vitoria.accountservice.domain.AccountGateway;
import com.vitoria.accountservice.domain.AuthToken;
import com.vitoria.accountservice.domain.PasswordHasher;
import com.vitoria.accountservice.domain.TokenGenerator;
import com.vitoria.accountservice.domain.entities.Account;
import com.vitoria.accountservice.domain.enums.AccountType;
import com.vitoria.accountservice.domain.exceptions.InvalidCredentialsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultAuthenticateUseCaseTest {
    @InjectMocks
    private DefaultAuthenticateUseCase useCase;
    @Mock
    private AccountGateway accountGateway;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private TokenGenerator tokenGenerator;

    @Test
    void givenValidCredentials_whenCallsAuthenticate_thenReturnsTokenAndAccountData() {
        final Account account =
                Account.newAccount("Ana Cliente", "ana@example.com", "hashed-password", AccountType.CLIENT);
        final AuthToken token = new AuthToken("jwt-token", 86400);
        final AuthenticateCommand command = new AuthenticateCommand("ana@example.com", "secret123");

        when(this.accountGateway.getByEmail("ana@example.com")).thenReturn(Optional.of(account));
        when(this.passwordHasher.matches("secret123", "hashed-password")).thenReturn(true);
        when(this.tokenGenerator.generate(account)).thenReturn(token);

        final AuthenticateOutput output = this.useCase.execute(command);

        assertNotNull(output);
        assertEquals("jwt-token", output.token());
        assertEquals(86400, output.expiresIn());
        assertEquals(account.getId(), output.accountId());
        assertEquals(account.getName(), output.name());
        assertEquals(account.getEmail(), output.email());
        assertEquals(account.getType(), output.type());
        verify(this.tokenGenerator, times(1)).generate(account);
    }

    @Test
    void givenUnknownEmail_whenCallsAuthenticate_thenThrowsInvalidCredentials() {
        final AuthenticateCommand command = new AuthenticateCommand("ana@example.com", "secret123");

        when(this.accountGateway.getByEmail("ana@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> this.useCase.execute(command));
        verify(this.passwordHasher, never()).matches(any(), any());
        verify(this.tokenGenerator, never()).generate(any());
    }

    @Test
    void givenInvalidPassword_whenCallsAuthenticate_thenThrowsInvalidCredentials() {
        final Account account =
                Account.newAccount("Ana Cliente", "ana@example.com", "hashed-password", AccountType.CLIENT);
        final AuthenticateCommand command = new AuthenticateCommand("ana@example.com", "wrong-password");

        when(this.accountGateway.getByEmail("ana@example.com")).thenReturn(Optional.of(account));
        when(this.passwordHasher.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> this.useCase.execute(command));
        verify(this.tokenGenerator, never()).generate(any());
    }
}
