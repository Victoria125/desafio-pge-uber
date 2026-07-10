package com.vitoria.rideservice.application.usecase.ride.changestatus;

import com.vitoria.rideservice.domain.enums.RideStatus;

public record ChangeStatusCommand(
        String rideId,
        RideStatus status
) {
}
