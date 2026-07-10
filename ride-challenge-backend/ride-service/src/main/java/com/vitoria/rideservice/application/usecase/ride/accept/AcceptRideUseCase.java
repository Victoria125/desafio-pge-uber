package com.vitoria.rideservice.application.usecase.ride.accept;

import com.vitoria.rideservice.application.usecase.ride.retrieve.getbyid.RideOutput;

public abstract class AcceptRideUseCase {
    public abstract RideOutput execute(AcceptRideCommand aCommand);
}
