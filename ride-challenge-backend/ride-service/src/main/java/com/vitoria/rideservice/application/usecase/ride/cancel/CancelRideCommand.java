package com.vitoria.rideservice.application.usecase.ride.cancel;

public record CancelRideCommand(
        String rideId,
        String userId
) {
}
