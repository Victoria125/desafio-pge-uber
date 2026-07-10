package com.vitoria.rideservice.infrastructure.api.models;

import jakarta.validation.constraints.NotBlank;

public record UpdateRideRequest(
        @NotBlank(message = "'userId' should be not blank")
        String userId,
        @NotBlank(message = "'startAddress' should be not blank")
        String startAddress,
        @NotBlank(message = "'destinationAddress' should be not blank")
        String destinationAddress
) {
}