package com.vitoria.rideservice.application.usecase.ride.retrieve.getstatus;

public abstract class GetRideStatusUseCase {
    public abstract RideStatusOutput execute(String anId);
}
