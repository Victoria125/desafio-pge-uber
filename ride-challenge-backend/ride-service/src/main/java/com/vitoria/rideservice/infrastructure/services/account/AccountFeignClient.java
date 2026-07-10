package com.vitoria.rideservice.infrastructure.services.account;

import com.vitoria.rideservice.infrastructure.services.account.models.AccountFeignResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "account-service")
public interface AccountFeignClient {

    @GetMapping("/accounts/{id}")
    AccountFeignResponse getAccountById(@PathVariable(name = "id") String id);
}
