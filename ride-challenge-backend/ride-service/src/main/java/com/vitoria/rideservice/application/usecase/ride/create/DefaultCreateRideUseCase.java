package com.vitoria.rideservice.application.usecase.ride.create;

import com.vitoria.rideservice.domain.AccountClient;
import com.vitoria.rideservice.domain.RideGateway;
import com.vitoria.rideservice.domain.SentEventService;
import com.vitoria.rideservice.domain.entities.Ride;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class DefaultCreateRideUseCase extends CreateRideUseCase {
    private static final Logger log = LoggerFactory.getLogger(DefaultCreateRideUseCase.class);

    private final RideGateway rideGateway;
    private final AccountClient accountClient;
    private final SentEventService<RideCreatedMessage> sentEventService;

    public DefaultCreateRideUseCase(
            final RideGateway rideGateway,
            final AccountClient accountClient,
            final SentEventService<RideCreatedMessage> sentEventService
    ) {
        this.rideGateway = Objects.requireNonNull(rideGateway);
        this.accountClient = Objects.requireNonNull(accountClient);
        this.sentEventService = Objects.requireNonNull(sentEventService);
    }

    @Override
    public CreateRideOutput execute(final CreateRideCommand aCommand) {
        final Ride aRide = Ride.newRide(
                aCommand.userId(),
                aCommand.startAddress(),
                aCommand.destinationAddress()
        );

        this.accountClient.getById(aCommand.userId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User with id %s does not exist".formatted(aCommand.userId())));

        final String anId = this.rideGateway.save(aRide);

        try {
            this.sentEventService.sentEvent(RideCreatedMessage.from(aRide));
        } catch (RuntimeException e) {
            log.error("Failed to publish ride created event to queue for ride {}: {}",
                    anId, e.getMessage(), e);
            throw e;
        }

        return new CreateRideOutput(anId);
    }
}
