package com.vitoria.rideservice.infrastructure.ride.persistence;

import com.vitoria.rideservice.domain.enums.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface RideRepository extends JpaRepository<RideEntity, String> {
    List<RideEntity> findTop50ByStatusAndCreatedAtBefore(RideStatus status, Instant limit);
}
