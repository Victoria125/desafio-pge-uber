package com.vitoria.accountservice.infrastructure.api;

import com.vitoria.accountservice.infrastructure.api.models.LoginRequest;
import com.vitoria.accountservice.infrastructure.api.models.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(value = "/auth")
public interface AuthAPI {

    @PostMapping(
            value = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest aRequest);
}
