package com.vitoria.rideservice.domain;

import java.util.Optional;

public interface AccountClient {
    Optional<AccountData> getById(String anId);
}
