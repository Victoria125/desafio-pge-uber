package com.vitoria.rideservice.application.usecase.ride.timeout;

import java.time.Instant;

public record TimeoutRidesCommand(Instant createdBefore) {
}
