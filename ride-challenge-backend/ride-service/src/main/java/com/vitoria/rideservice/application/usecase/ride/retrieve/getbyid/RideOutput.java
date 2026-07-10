package com.vitoria.rideservice.application.usecase.ride.retrieve.getbyid;

import com.vitoria.rideservice.domain.entities.Ride;
import com.vitoria.rideservice.domain.enums.RideStatus;

import java.time.Instant;

public record RideOutput(
        String id,
        String userId,
        String driverId,
        String startAddress,
        String destinationAddress,
        RideStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static RideOutput from(final Ride aRide) {
        return new RideOutput(
                aRide.getId(),
                aRide.getUserId(),
                aRide.getDriverId(),
                aRide.getStartAddress(),
                aRide.getDestinationAddress(),
                aRide.getStatus(),
                aRide.getCreatedAt(),
                aRide.getUpdatedAt()
        );
    }
}
