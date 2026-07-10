package com.vitoria.rideservice.application.usecase.ride.timeout;

public abstract class TimeoutRidesUseCase {
    public abstract int execute(TimeoutRidesCommand aCommand);
}
