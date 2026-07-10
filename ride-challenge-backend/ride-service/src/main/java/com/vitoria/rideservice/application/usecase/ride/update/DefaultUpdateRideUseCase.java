package com.vitoria.rideservice.application.usecase.ride.update;

import com.vitoria.rideservice.application.usecase.ride.retrieve.getbyid.RideOutput;
import com.vitoria.rideservice.domain.DriverNotifier;
import com.vitoria.rideservice.domain.RideGateway;
import com.vitoria.rideservice.domain.RideNotification;
import com.vitoria.rideservice.domain.entities.Ride;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.Objects;

public class DefaultUpdateRideUseCase extends UpdateRideUseCase {
    private static final Logger log = LoggerFactory.getLogger(DefaultUpdateRideUseCase.class);

    private final RideGateway rideGateway;
    private final DriverNotifier driverNotifier;

    public DefaultUpdateRideUseCase(
            final RideGateway rideGateway,
            final DriverNotifier driverNotifier
    ) {
        this.rideGateway = Objects.requireNonNull(rideGateway);
        this.driverNotifier = Objects.requireNonNull(driverNotifier);
    }

    @Override
    public RideOutput execute(final UpdateRideCommand aCommand) {
        if (aCommand.userId() == null || aCommand.userId().isBlank()) {
            throw new IllegalArgumentException("'userId' should be not blank");
        }

        final Ride aRide = this.rideGateway.getById(aCommand.rideId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Ride with id %s was not found".formatted(aCommand.rideId())));

        if (!aRide.getUserId().equals(aCommand.userId())) {
            throw new IllegalArgumentException(
                    "User %s cannot edit ride %s".formatted(aCommand.userId(), aCommand.rideId()));
        }

        aRide.updateRoute(aCommand.startAddress(), aCommand.destinationAddress());
        this.rideGateway.save(aRide);

        try {
            this.driverNotifier.notify(new RideNotification(
                    aRide.getId(),
                    aRide.getUserId(),
                    aRide.getDriverId(),
                    aRide.getStartAddress(),
                    aRide.getDestinationAddress(),
                    aRide.getStatus().name()
            ));
        } catch (RuntimeException e) {
            log.error("Failed to notify drivers about ride {} update: {}", aRide.getId(), e.getMessage(), e);
        }

        return RideOutput.from(aRide);
    }
}