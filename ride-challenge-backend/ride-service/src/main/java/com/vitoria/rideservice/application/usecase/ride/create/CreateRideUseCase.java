package com.vitoria.rideservice.application.usecase.ride.create;

public abstract class CreateRideUseCase {
    public abstract CreateRideOutput execute(CreateRideCommand aCommand);
}
