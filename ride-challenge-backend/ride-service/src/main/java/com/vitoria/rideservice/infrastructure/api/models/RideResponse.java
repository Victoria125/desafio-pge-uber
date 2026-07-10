package com.vitoria.rideservice.infrastructure.api.models;

import com.vitoria.rideservice.domain.enums.RideStatus;

import java.time.Instant;

public record RideResponse(
        String id,
        String userId,
        String driverId,
        String startAddress,
        String destinationAddress,
        RideStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
