package com.vitoria.accountservice.infrastructure.api.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "'email' should be not blank")
        @Email(message = "'email' should be a valid email")
        String email,

        @NotBlank(message = "'password' should be not blank")
        String password
) {
}
