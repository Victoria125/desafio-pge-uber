package com.vitoria.rideservice.application.usecase.ride.update;

public record UpdateRideCommand(
        String rideId,
        String userId,
        String startAddress,
        String destinationAddress
) {
}