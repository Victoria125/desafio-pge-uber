package com.vitoria.rideservice.application.usecase.ride.retrieve.list;

import com.vitoria.rideservice.domain.RideGateway;

import java.util.List;
import java.util.Objects;

public class DefaultListRidesUseCase extends ListRidesUseCase {
    private final RideGateway rideGateway;

    public DefaultListRidesUseCase(final RideGateway rideGateway) {
        this.rideGateway = Objects.requireNonNull(rideGateway);
    }

    @Override
    public List<ListRidesOutput> execute() {
        return this.rideGateway.getAll()
                .stream()
                .map(ListRidesOutput::from)
                .toList();
    }
}
