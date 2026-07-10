package com.vitoria.rideservice.infrastructure.ride;

import com.vitoria.rideservice.domain.RideGateway;
import com.vitoria.rideservice.domain.entities.Ride;
import com.vitoria.rideservice.domain.enums.RideStatus;
import com.vitoria.rideservice.infrastructure.ride.persistence.RideEntity;
import com.vitoria.rideservice.infrastructure.ride.persistence.RideRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class RidePostgresGateway implements RideGateway {
    private final RideRepository rideRepository;

    public RidePostgresGateway(final RideRepository rideRepository) {
        this.rideRepository = Objects.requireNonNull(rideRepository);
    }

    @Override
    public String save(final Ride aRide) {
        final RideEntity anEntity = RideEntity.from(aRide);
        return this.rideRepository.save(anEntity).getId();
    }

    @Override
    public Optional<Ride> getById(final String anId) {
        return this.rideRepository.findById(anId)
                .map(RideEntity::toAggregate);
    }

    @Override
    public List<Ride> getAll() {
        return this.rideRepository
                .findAll()
                .stream()
                .map(RideEntity::toAggregate)
                .toList();
    }

    @Override
    public List<Ride> findCreatedBefore(final Instant aLimit) {
        return this.rideRepository
                .findTop50ByStatusAndCreatedAtBefore(RideStatus.CREATED, aLimit)
                .stream()
                .map(RideEntity::toAggregate)
                .toList();
    }
}
