package com.vitoria.rideservice.application.usecase.ride.timeout;

import com.vitoria.rideservice.domain.DriverNotifier;
import com.vitoria.rideservice.domain.RideGateway;
import com.vitoria.rideservice.domain.RideNotification;
import com.vitoria.rideservice.domain.RideStatusCache;
import com.vitoria.rideservice.domain.entities.Ride;
import com.vitoria.rideservice.domain.enums.RideStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class DefaultTimeoutRidesUseCase extends TimeoutRidesUseCase {
    private static final Logger log = LoggerFactory.getLogger(DefaultTimeoutRidesUseCase.class);

    private final RideGateway rideGateway;
    private final RideStatusCache rideStatusCache;
    private final DriverNotifier driverNotifier;

    public DefaultTimeoutRidesUseCase(
            final RideGateway rideGateway,
            final RideStatusCache rideStatusCache,
            final DriverNotifier driverNotifier
    ) {
        this.rideGateway = Objects.requireNonNull(rideGateway);
        this.rideStatusCache = Objects.requireNonNull(rideStatusCache);
        this.driverNotifier = Objects.requireNonNull(driverNotifier);
    }

    @Override
    public int execute(final TimeoutRidesCommand aCommand) {
        final List<Ride> expiredRides = this.rideGateway.findCreatedBefore(aCommand.createdBefore());
        int cancelled = 0;

        
        for (final Ride aRide : expiredRides) {
            try {
                aRide.changeStatus(RideStatus.CANCELLED);
                this.rideGateway.save(aRide);
                this.rideStatusCache.put(aRide.getId(), aRide.getStatus(), aRide.getDriverId());
                this.driverNotifier.notify(new RideNotification(
                        aRide.getId(),
                        aRide.getUserId(),
                        aRide.getDriverId(),
                        aRide.getStartAddress(),
                        aRide.getDestinationAddress(),
                        aRide.getStatus().name()
                ));
                cancelled++;
            } catch (RuntimeException e) {
                log.warn("Erro ao cancelar corrida expirada {}: {}", aRide.getId(), e.getMessage());
            }
        }
        return cancelled;
    }
}
