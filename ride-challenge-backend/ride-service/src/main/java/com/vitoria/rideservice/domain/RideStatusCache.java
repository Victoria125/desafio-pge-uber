package com.vitoria.rideservice.domain;

import com.vitoria.rideservice.domain.enums.RideStatus;

import java.util.Optional;

public interface RideStatusCache {
    void put(String rideId, RideStatus status, String driverId);

    Optional<RideStatusData> get(String rideId);
}
