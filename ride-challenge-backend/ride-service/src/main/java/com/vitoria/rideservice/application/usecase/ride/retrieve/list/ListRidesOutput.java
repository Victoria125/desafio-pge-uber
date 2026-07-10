package com.vitoria.rideservice.application.usecase.ride.retrieve.list;

import com.vitoria.rideservice.domain.entities.Ride;
import com.vitoria.rideservice.domain.enums.RideStatus;

import java.time.Instant;

public record ListRidesOutput(
        String id,
        String userId,
        String driverId,
        String startAddress,
        String destinationAddress,
        RideStatus status,
        Instant createdAt
) {
    public static ListRidesOutput from(final Ride aRide) {
        return new ListRidesOutput(
                aRide.getId(),
                aRide.getUserId(),
                aRide.getDriverId(),
                aRide.getStartAddress(),
                aRide.getDestinationAddress(),
                aRide.getStatus(),
                aRide.getCreatedAt()
        );
    }
}
