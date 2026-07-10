package com.vitoria.rideservice.application.usecase.ride.changestatus;

import com.vitoria.rideservice.domain.RideGateway;
import com.vitoria.rideservice.domain.entities.Ride;

import java.util.NoSuchElementException;
import java.util.Objects;

public class DefaultChangeStatusRideUseCase extends ChangeStatusRideUseCase {
    private final RideGateway rideGateway;

    public DefaultChangeStatusRideUseCase(final RideGateway rideGateway) {
        this.rideGateway = Objects.requireNonNull(rideGateway);
    }

    @Override
    public void execute(final ChangeStatusCommand aCommand) {
        final Ride aRide = this.rideGateway.getById(aCommand.rideId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Ride with id %s was not found".formatted(aCommand.rideId())));
        aRide.changeStatus(aCommand.status());
        this.rideGateway.save(aRide);
    }
}
