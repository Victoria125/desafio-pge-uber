package com.vitoria.rideservice.infrastructure.ride.persistence;

import com.vitoria.rideservice.domain.entities.Ride;
import com.vitoria.rideservice.domain.enums.RideStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Table(name = "tb_rides")
@Entity
public class RideEntity {
    @Id
    @Column(name = "ride_id", nullable = false)
    private String id;

    @Column(name = "ride_user_id", nullable = false)
    private String userId;

    @Column(name = "ride_driver_id")
    private String driverId;

    @Column(name = "ride_start_address", nullable = false)
    private String startAddress;

    @Column(name = "ride_destination_address", nullable = false)
    private String destinationAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "ride_status", nullable = false)
    private RideStatus status;

    @Column(name = "ride_created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "ride_updated_at", nullable = false)
    private Instant updatedAt;

    public static RideEntity from(final Ride aRide) {
        return new RideEntity(
                aRide.getId(),
                aRide.getUserId(),
                aRide.getDriverId(),
                aRide.getStartAddress(),
                aRide.getDestinationAddress(),
                aRide.getStatus(),
                aRide.getCreatedAt(),
                aRide.getUpdatedAt()
        );
    }

    public static Ride toAggregate(final RideEntity anEntity) {
        return Ride.with(
                anEntity.getId(),
                anEntity.getUserId(),
                anEntity.getDriverId(),
                anEntity.getStartAddress(),
                anEntity.getDestinationAddress(),
                anEntity.getStatus(),
                anEntity.getCreatedAt(),
                anEntity.getUpdatedAt()
        );
    }

    private RideEntity(
            final String id,
            final String userId,
            final String driverId,
            final String startAddress,
            final String destinationAddress,
            final RideStatus status,
            final Instant createdAt,
            final Instant updatedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.driverId = driverId;
        this.startAddress = startAddress;
        this.destinationAddress = destinationAddress;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public RideEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public RideStatus getStatus() {
        return status;
    }

    public void setStatus(RideStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
