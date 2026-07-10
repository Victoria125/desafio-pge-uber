package com.vitoria.rideservice.application.usecase.ride.retrieve.getbyid;

import com.vitoria.rideservice.domain.RideGateway;

import java.util.NoSuchElementException;
import java.util.Objects;

public class DefaultGetRideByIdUseCase extends GetRideByIdUseCase {
    private final RideGateway rideGateway;

    public DefaultGetRideByIdUseCase(final RideGateway rideGateway) {
        this.rideGateway = Objects.requireNonNull(rideGateway);
    }

    @Override
    public RideOutput execute(final String anId) {
        return this.rideGateway.getById(anId)
                .map(RideOutput::from)
                .orElseThrow(() -> new NoSuchElementException(
                        "Ride with id %s was not found".formatted(anId)));
    }
}
