package com.vitoria.rideservice.domain.entities;

import com.vitoria.rideservice.domain.enums.RideStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RideTest {

    @Test
    void givenAValidParams_whenCallsNewRide_thenInstantiateARide() {
        final String expectedUserId = UUID.randomUUID().toString();
        final String expectedStartAddress = "Rua A, 100 - Centro";
        final String expectedDestinationAddress = "Av. B, 200 - Aldeota";

        final Ride aRide = Ride.newRide(expectedUserId, expectedStartAddress, expectedDestinationAddress);

        assertNotNull(aRide);
        assertNotNull(aRide.getId());
        assertNotNull(aRide.getCreatedAt());
        assertNotNull(aRide.getUpdatedAt());
        assertNull(aRide.getDriverId());
        assertEquals(expectedUserId, aRide.getUserId());
        assertEquals(expectedStartAddress, aRide.getStartAddress());
        assertEquals(expectedDestinationAddress, aRide.getDestinationAddress());
        assertEquals(RideStatus.CREATED, aRide.getStatus());
    }

    @Test
    void givenAnInvalidNullUserId_whenCallsNewRide_thenThrowsException() {
        final String expectedMessageError = "'userId' should be not null";

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Ride.newRide(null, "Rua A, 100", "Av. B, 200"));

        assertEquals(expectedMessageError, exception.getMessage());
    }

    @Test
    void givenAnInvalidBlankStartAddress_whenCallsNewRide_thenThrowsException() {
        final String expectedMessageError = "'startAddress' should be not blank";

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Ride.newRide(UUID.randomUUID().toString(), "   ", "Av. B, 200"));

        assertEquals(expectedMessageError, exception.getMessage());
    }

    @Test
    void givenAnInvalidNullStartAddress_whenCallsNewRide_thenThrowsException() {
        final String expectedMessageError = "'startAddress' should be not null";

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Ride.newRide(UUID.randomUUID().toString(), null, "Av. B, 200"));

        assertEquals(expectedMessageError, exception.getMessage());
    }

    @Test
    void givenAnInvalidBlankDestinationAddress_whenCallsNewRide_thenThrowsException() {
        final String expectedMessageError = "'destinationAddress' should be not blank";

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Ride.newRide(UUID.randomUUID().toString(), "Rua A, 100", ""));

        assertEquals(expectedMessageError, exception.getMessage());
    }

    @Test
    void givenAnInvalidNullDestinationAddress_whenCallsNewRide_thenThrowsException() {
        final String expectedMessageError = "'destinationAddress' should be not null";

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Ride.newRide(UUID.randomUUID().toString(), "Rua A, 100", null));

        assertEquals(expectedMessageError, exception.getMessage());
    }

    @Test
    void givenACreatedRide_whenCallsAssignDriver_thenBindDriverAndSetInProgress() {
        final Ride aRide = Ride.newRide(UUID.randomUUID().toString(), "Rua A, 100", "Av. B, 200");
        final String expectedDriverId = UUID.randomUUID().toString();

        aRide.assignDriver(expectedDriverId);

        assertEquals(expectedDriverId, aRide.getDriverId());
        assertEquals(RideStatus.IN_PROGRESS, aRide.getStatus());
    }

    @Test
    void givenACreatedRide_whenCallsAssignDriverWithBlankId_thenThrowsException() {
        final Ride aRide = Ride.newRide(UUID.randomUUID().toString(), "Rua A, 100", "Av. B, 200");
        final String expectedMessageError = "'driverId' should be not blank";

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> aRide.assignDriver("  "));

        assertEquals(expectedMessageError, exception.getMessage());
        assertEquals(RideStatus.CREATED, aRide.getStatus());
    }

    @Test
    void givenANotFinishedRide_whenCallsUpdateRoute_thenUpdateOriginAndDestination() {
        final Ride aRide = Ride.newRide(UUID.randomUUID().toString(), "Rua A, 100", "Av. B, 200");

        aRide.updateRoute("Rua C, 300", "Av. D, 400");

        assertEquals("Rua C, 300", aRide.getStartAddress());
        assertEquals("Av. D, 400", aRide.getDestinationAddress());
        assertEquals(RideStatus.CREATED, aRide.getStatus());
        assertNotNull(aRide.getUpdatedAt());
    }

    @Test
    void givenACompletedRide_whenCallsUpdateRoute_thenThrowsException() {
        final Ride aRide = Ride.newRide(UUID.randomUUID().toString(), "Rua A, 100", "Av. B, 200");
        aRide.changeStatus(RideStatus.COMPLETED);
        final String expectedMessageError = "Ride %s can no longer be edited".formatted(aRide.getId());

        final IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> aRide.updateRoute("Rua C, 300", "Av. D, 400"));

        assertEquals(expectedMessageError, exception.getMessage());
        assertEquals("Rua A, 100", aRide.getStartAddress());
        assertEquals("Av. B, 200", aRide.getDestinationAddress());
    }

    @Test
    void givenACancelledRide_whenCallsUpdateRoute_thenThrowsException() {
        final Ride aRide = Ride.newRide(UUID.randomUUID().toString(), "Rua A, 100", "Av. B, 200");
        aRide.changeStatus(RideStatus.CANCELLED);
        final String expectedMessageError = "Ride %s can no longer be edited".formatted(aRide.getId());

        final IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> aRide.updateRoute("Rua C, 300", "Av. D, 400"));

        assertEquals(expectedMessageError, exception.getMessage());
        assertEquals("Rua A, 100", aRide.getStartAddress());
        assertEquals("Av. B, 200", aRide.getDestinationAddress());
    }

    @Test
    void givenARide_whenCallsChangeStatus_thenUpdateStatus() {
        final Ride aRide = Ride.newRide(UUID.randomUUID().toString(), "Rua A, 100", "Av. B, 200");

        aRide.changeStatus(RideStatus.COMPLETED);

        assertEquals(RideStatus.COMPLETED, aRide.getStatus());
    }
}