package com.vitoria.rideservice.application.usecase.ride.create;

public record CreateRideCommand(
        String userId,
        String startAddress,
        String destinationAddress
) {
}
