package com.vitoria.accountservice.infrastructure.api.models;

import com.vitoria.accountservice.domain.enums.AccountType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(
        @NotBlank(message = "'name' should be not blank")
        String name,

        @NotBlank(message = "'email' should be not blank")
        @Email(message = "'email' should be a valid email")
        String email,

        @NotNull(message = "'type' should be CLIENT or DRIVER")
        AccountType type
) {
}
