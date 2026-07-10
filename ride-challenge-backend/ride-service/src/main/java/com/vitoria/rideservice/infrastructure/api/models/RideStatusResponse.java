package com.vitoria.rideservice.infrastructure.api.models;

public record RideStatusResponse(
        String rideId,
        String status,
        String driverId,
        String source
) {
}
