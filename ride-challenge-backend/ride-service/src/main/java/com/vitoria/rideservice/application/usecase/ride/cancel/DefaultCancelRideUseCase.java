package com.vitoria.rideservice.application.usecase.ride.cancel;

import com.vitoria.rideservice.application.usecase.ride.retrieve.getbyid.RideOutput;
import com.vitoria.rideservice.domain.DriverNotifier;
import com.vitoria.rideservice.domain.RideGateway;
import com.vitoria.rideservice.domain.RideNotification;
import com.vitoria.rideservice.domain.RideStatusCache;
import com.vitoria.rideservice.domain.entities.Ride;
import com.vitoria.rideservice.domain.exceptions.ForbiddenOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.Objects;

public class DefaultCancelRideUseCase extends CancelRideUseCase {
    private static final Logger log = LoggerFactory.getLogger(DefaultCancelRideUseCase.class);

    private final RideGateway rideGateway;
    private final RideStatusCache rideStatusCache;
    private final DriverNotifier driverNotifier;

    public DefaultCancelRideUseCase(
            final RideGateway rideGateway,
            final RideStatusCache rideStatusCache,
            final DriverNotifier driverNotifier
    ) {
        this.rideGateway = Objects.requireNonNull(rideGateway);
        this.rideStatusCache = Objects.requireNonNull(rideStatusCache);
        this.driverNotifier = Objects.requireNonNull(driverNotifier);
    }

    @Override
    public RideOutput execute(final CancelRideCommand aCommand) {
        if (aCommand.userId() == null || aCommand.userId().isBlank()) {
            throw new IllegalArgumentException("'userId' should be not blank");
        }

        final Ride aRide = this.rideGateway.getById(aCommand.rideId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Ride with id %s was not found".formatted(aCommand.rideId())));

        if (!aRide.getUserId().equals(aCommand.userId())) {
            throw new ForbiddenOperationException(
                    "User %s cannot cancel ride %s".formatted(aCommand.userId(), aCommand.rideId()));
        }

        aRide.cancel();
        this.rideGateway.save(aRide);

        try {
            this.rideStatusCache.put(aRide.getId(), aRide.getStatus(), aRide.getDriverId());
        } catch (RuntimeException e) {
            log.error("Falha ao gravar status da corrida {} no Redis: {}", aRide.getId(), e.getMessage(), e);
        }

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
            log.error("Falha ao notificar motoristas sobre o cancelamento da corrida {}: {}",
                    aRide.getId(), e.getMessage(), e);
        }

        return RideOutput.from(aRide);
    }
}
