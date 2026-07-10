package com.vitoria.rideservice.infrastructure.services.account;

import com.vitoria.rideservice.domain.AccountClient;
import com.vitoria.rideservice.domain.AccountData;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class AccountClientImpl implements AccountClient {
    private static final Logger log = LoggerFactory.getLogger(AccountClientImpl.class);
    private final AccountFeignClient accountFeignClient;

    public AccountClientImpl(final AccountFeignClient accountFeignClient) {
        this.accountFeignClient = Objects.requireNonNull(accountFeignClient);
    }

    @Override
    public Optional<AccountData> getById(final String anId) {
        try {
            final var response = this.accountFeignClient.getAccountById(anId);
            return Optional.of(new AccountData(response.id(), response.name(), response.type()));
        } catch (FeignException.NotFound e) {
            log.info("Account {} nao encontrada no account-service", anId);
            return Optional.empty();
        }
    }
}
