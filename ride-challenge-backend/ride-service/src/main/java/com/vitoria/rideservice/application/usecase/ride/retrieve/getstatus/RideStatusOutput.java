package com.vitoria.rideservice.application.usecase.ride.retrieve.getstatus;

public record RideStatusOutput(
        String rideId,
        String status,
        String driverId,
        String source
) {
}
