package com.vitoria.rideservice.infrastructure.api.models;

import jakarta.validation.constraints.NotBlank;

public record AcceptRideRequest(
        @NotBlank(message = "'driverId' should be not blank")
        String driverId
) {
}
