package com.vitoria.rideservice.application.usecase.ride.accept;

import com.vitoria.rideservice.application.usecase.ride.retrieve.getbyid.RideOutput;
import com.vitoria.rideservice.domain.*;
import com.vitoria.rideservice.domain.entities.Ride;
import com.vitoria.rideservice.domain.enums.RideStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.Objects;

public class DefaultAcceptRideUseCase extends AcceptRideUseCase {
    private static final Logger log = LoggerFactory.getLogger(DefaultAcceptRideUseCase.class);

    private final RideGateway rideGateway;
    private final AccountClient accountClient;
    private final RideStatusCache rideStatusCache;
    private final DriverNotifier driverNotifier;

    public DefaultAcceptRideUseCase(
            final RideGateway rideGateway,
            final AccountClient accountClient,
            final RideStatusCache rideStatusCache,
            final DriverNotifier driverNotifier
    ) {
        this.rideGateway = Objects.requireNonNull(rideGateway);
        this.accountClient = Objects.requireNonNull(accountClient);
        this.rideStatusCache = Objects.requireNonNull(rideStatusCache);
        this.driverNotifier = Objects.requireNonNull(driverNotifier);
    }

    @Override
    public RideOutput execute(final AcceptRideCommand aCommand) {
        final AccountData anAccount = this.accountClient.getById(aCommand.driverId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Driver with id %s does not exist".formatted(aCommand.driverId())));

        if (!"DRIVER".equals(anAccount.type())) {
            throw new IllegalArgumentException(
                    "Account %s is not a driver".formatted(aCommand.driverId()));
        }

        final Ride aRide = this.rideGateway.getById(aCommand.rideId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Ride with id %s was not found".formatted(aCommand.rideId())));

        if (aRide.getStatus() != RideStatus.CREATED) {
            throw new IllegalStateException(
                    "Ride %s was already accepted or finished".formatted(aCommand.rideId()));
        }

        aRide.assignDriver(aCommand.driverId());
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
            log.error("Falha ao notificar motoristas sobre a corrida {}: {}", aRide.getId(), e.getMessage(), e);
        }

        return RideOutput.from(aRide);
    }
}
