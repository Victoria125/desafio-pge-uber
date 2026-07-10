package com.vitoria.rideservice.domain.entities;

import com.vitoria.rideservice.domain.enums.RideStatus;

import java.time.Instant;
import java.util.UUID;

public class Ride {
    private String id;
    private String userId;
    private String driverId;
    private String startAddress;
    private String destinationAddress;
    private RideStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public static Ride with(
            String id,
            String userId,
            String driverId,
            String startAddress,
            String destinationAddress,
            RideStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Ride(id, userId, driverId, startAddress, destinationAddress, status, createdAt, updatedAt);
    }

    public static Ride newRide(
            final String aUserId,
            final String aStartAddress,
            final String aDestinationAddress
    ) {
        validate(aUserId, aStartAddress, aDestinationAddress);
        final String anId = UUID.randomUUID().toString();
        final Instant anInstant = Instant.now();
        return new Ride(anId, aUserId, null, aStartAddress, aDestinationAddress,
                RideStatus.CREATED, anInstant, anInstant);
    }

    public Ride assignDriver(final String aDriverId) {
        if (aDriverId == null || aDriverId.isBlank()) {
            throw new IllegalArgumentException("'driverId' should be not blank");
        }
        this.driverId = aDriverId;
        this.status = RideStatus.IN_PROGRESS;
        this.updatedAt = Instant.now();
        return this;
    }

    public Ride updateRoute(final String aStartAddress, final String aDestinationAddress) {
        if (this.status == RideStatus.COMPLETED || this.status == RideStatus.CANCELLED) {
            throw new IllegalStateException("Ride %s can no longer be edited".formatted(this.id));
        }
        validateRoute(aStartAddress, aDestinationAddress);
        this.startAddress = aStartAddress;
        this.destinationAddress = aDestinationAddress;
        this.updatedAt = Instant.now();
        return this;
    }

    public Ride changeStatus(final RideStatus aStatus) {
        this.updatedAt = Instant.now();
        this.status = aStatus;
        return this;
    }

    private static void validate(
            final String aUserId,
            final String aStartAddress,
            final String aDestinationAddress
    ) {
        if (aUserId == null) {
            throw new IllegalArgumentException("'userId' should be not null");
        }

        if (aUserId.isBlank()) {
            throw new IllegalArgumentException("'userId' should be not blank");
        }

        validateRoute(aStartAddress, aDestinationAddress);
    }

    private static void validateRoute(
            final String aStartAddress,
            final String aDestinationAddress
    ) {
        if (aStartAddress == null) {
            throw new IllegalArgumentException("'startAddress' should be not null");
        }

        if (aStartAddress.isBlank()) {
            throw new IllegalArgumentException("'startAddress' should be not blank");
        }

        if (aDestinationAddress == null) {
            throw new IllegalArgumentException("'destinationAddress' should be not null");
        }

        if (aDestinationAddress.isBlank()) {
            throw new IllegalArgumentException("'destinationAddress' should be not blank");
        }
    }

    private Ride(
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

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getDriverId() {
        return driverId;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public RideStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}