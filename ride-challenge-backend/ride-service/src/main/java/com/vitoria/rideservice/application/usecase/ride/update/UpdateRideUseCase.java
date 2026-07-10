package com.vitoria.rideservice.application.usecase.ride.update;

import com.vitoria.rideservice.application.usecase.ride.retrieve.getbyid.RideOutput;

public abstract class UpdateRideUseCase {
    public abstract RideOutput execute(UpdateRideCommand aCommand);
}