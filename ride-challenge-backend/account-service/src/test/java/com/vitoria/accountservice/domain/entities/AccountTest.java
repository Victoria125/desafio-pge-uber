package com.vitoria.accountservice.domain.entities;

import com.vitoria.accountservice.domain.enums.AccountType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void givenAValidParams_whenCallsNewAccount_thenInstantiateAnAccount() {
        
        final String expectedName = "Maria Silva";
        final String expectedEmail = "maria@email.com";
        final AccountType expectedType = AccountType.CLIENT;

        
        final Account anAccount = Account.newAccount(expectedName, expectedEmail, expectedType);

        
        assertNotNull(anAccount);
        assertNotNull(anAccount.getId());
        assertNotNull(anAccount.getCreatedAt());
        assertEquals(expectedName, anAccount.getName());
        assertEquals(expectedEmail, anAccount.getEmail());
        assertEquals(expectedType, anAccount.getType());
    }

    @Test
    void givenAnInvalidNullName_whenCallsNewAccount_thenThrowsException() {
        
        final String expectedMessageError = "'name' should be not null";

        
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Account.newAccount(null, "maria@email.com", AccountType.CLIENT));

        
        assertEquals(expectedMessageError, exception.getMessage());
    }

    @Test
    void givenAnInvalidBlankName_whenCallsNewAccount_thenThrowsException() {
        
        final String expectedMessageError = "'name' should be not blank";

        
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Account.newAccount("   ", "maria@email.com", AccountType.CLIENT));

        
        assertEquals(expectedMessageError, exception.getMessage());
    }

    @Test
    void givenAnInvalidNullEmail_whenCallsNewAccount_thenThrowsException() {
        
        final String expectedMessageError = "'email' should be not null";

        
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Account.newAccount("Maria Silva", null, AccountType.CLIENT));

        
        assertEquals(expectedMessageError, exception.getMessage());
    }

    @Test
    void givenAnInvalidBlankEmail_whenCallsNewAccount_thenThrowsException() {
        
        final String expectedMessageError = "'email' should be not blank";

        
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Account.newAccount("Maria Silva", "  ", AccountType.CLIENT));

        
        assertEquals(expectedMessageError, exception.getMessage());
    }

    @Test
    void givenAnInvalidNullType_whenCallsNewAccount_thenThrowsException() {
        
        final String expectedMessageError = "'type' should be not null";

        
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Account.newAccount("Maria Silva", "maria@email.com", null));

        
        assertEquals(expectedMessageError, exception.getMessage());
    }
}
