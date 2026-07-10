package com.vitoria.rideservice.domain;

public record RideNotification(
        String rideId,
        String userId,
        String driverId,
        String startAddress,
        String destinationAddress,
        String status
) {
}
