package com.vitoria.rideservice.application.usecase.ride.accept;

public record AcceptRideCommand(
        String rideId,
        String driverId
) {
}
