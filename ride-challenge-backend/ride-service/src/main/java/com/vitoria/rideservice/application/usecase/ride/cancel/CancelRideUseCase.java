package com.vitoria.rideservice.application.usecase.ride.cancel;

import com.vitoria.rideservice.application.usecase.ride.retrieve.getbyid.RideOutput;

public abstract class CancelRideUseCase {
    public abstract RideOutput execute(CancelRideCommand aCommand);
}
