package com.vitoria.accountservice.infrastructure.api.controllers;

import com.vitoria.accountservice.application.usecase.auth.AuthenticateCommand;
import com.vitoria.accountservice.application.usecase.auth.AuthenticateOutput;
import com.vitoria.accountservice.application.usecase.auth.AuthenticateUseCase;
import com.vitoria.accountservice.infrastructure.api.AuthAPI;
import com.vitoria.accountservice.infrastructure.api.models.LoginRequest;
import com.vitoria.accountservice.infrastructure.api.models.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class AuthController implements AuthAPI {
    private final AuthenticateUseCase authenticateUseCase;

    public AuthController(final AuthenticateUseCase authenticateUseCase) {
        this.authenticateUseCase = Objects.requireNonNull(authenticateUseCase);
    }

    @Override
    public ResponseEntity<LoginResponse> login(final LoginRequest aRequest) {
        final AuthenticateOutput anOutput = this.authenticateUseCase.execute(
                new AuthenticateCommand(aRequest.email(), aRequest.password()));
        return ResponseEntity.ok(new LoginResponse(
                anOutput.token(),
                anOutput.expiresIn(),
                anOutput.accountId(),
                anOutput.name(),
                anOutput.email(),
                anOutput.type()
        ));
    }
}
