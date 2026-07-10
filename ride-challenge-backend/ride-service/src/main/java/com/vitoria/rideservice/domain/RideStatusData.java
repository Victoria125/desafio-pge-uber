package com.vitoria.rideservice.domain;

public record RideStatusData(
        String rideId,
        String status,
        String driverId
) {
}
