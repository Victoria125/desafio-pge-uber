package com.vitoria.rideservice.application.usecase.ride.retrieve.getstatus;

import com.vitoria.rideservice.domain.RideGateway;
import com.vitoria.rideservice.domain.RideStatusCache;
import com.vitoria.rideservice.domain.entities.Ride;

import java.util.NoSuchElementException;
import java.util.Objects;

public class DefaultGetRideStatusUseCase extends GetRideStatusUseCase {
    private final RideStatusCache rideStatusCache;
    private final RideGateway rideGateway;

    public DefaultGetRideStatusUseCase(
            final RideStatusCache rideStatusCache,
            final RideGateway rideGateway
    ) {
        this.rideStatusCache = Objects.requireNonNull(rideStatusCache);
        this.rideGateway = Objects.requireNonNull(rideGateway);
    }

    @Override
    public RideStatusOutput execute(final String anId) {
        
        return this.rideStatusCache.get(anId)
                .map(cached -> new RideStatusOutput(anId, cached.status(), cached.driverId(), "redis"))
                .orElseGet(() -> {
                    final Ride aRide = this.rideGateway.getById(anId)
                            .orElseThrow(() -> new NoSuchElementException(
                                    "Ride with id %s was not found".formatted(anId)));
                    return new RideStatusOutput(anId, aRide.getStatus().name(), aRide.getDriverId(), "database");
                });
    }
}
