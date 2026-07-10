package com.vitoria.rideservice.domain;

import com.vitoria.rideservice.domain.entities.Ride;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RideGateway {
    String save(Ride aRide);

    Optional<Ride> getById(String anId);

    List<Ride> getAll();

    List<Ride> findCreatedBefore(Instant aLimit);
}
