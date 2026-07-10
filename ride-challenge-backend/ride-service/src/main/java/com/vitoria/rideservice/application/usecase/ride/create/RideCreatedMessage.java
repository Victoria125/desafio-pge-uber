package com.vitoria.rideservice.application.usecase.ride.create;

import com.vitoria.rideservice.domain.entities.Ride;

public record RideCreatedMessage(
        String rideId,
        String userId,
        String startAddress,
        String destinationAddress
) {
    public static RideCreatedMessage from(final Ride aRide) {
        return new RideCreatedMessage(
                aRide.getId(),
                aRide.getUserId(),
                aRide.getStartAddress(),
                aRide.getDestinationAddress()
        );
    }
}
