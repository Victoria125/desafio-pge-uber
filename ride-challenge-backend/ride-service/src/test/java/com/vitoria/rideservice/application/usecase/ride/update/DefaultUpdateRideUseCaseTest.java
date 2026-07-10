package com.vitoria.rideservice.application.usecase.ride.update;

import com.vitoria.rideservice.application.usecase.ride.retrieve.getbyid.RideOutput;
import com.vitoria.rideservice.domain.DriverNotifier;
import com.vitoria.rideservice.domain.RideGateway;
import com.vitoria.rideservice.domain.RideNotification;
import com.vitoria.rideservice.domain.entities.Ride;
import com.vitoria.rideservice.domain.enums.RideStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultUpdateRideUseCaseTest {
    @InjectMocks
    private DefaultUpdateRideUseCase useCase;
    @Mock
    private RideGateway rideGateway;
    @Mock
    private DriverNotifier driverNotifier;

    @Test
    void givenAValidOwnerAndOpenRide_whenCallsUpdateRide_thenUpdateRouteAndNotifyDrivers() {
        final Ride aRide = Ride.newRide(UUID.randomUUID().toString(), "Rua A, 100", "Av. B, 200");
        final UpdateRideCommand aCommand = new UpdateRideCommand(
                aRide.getId(),
                aRide.getUserId(),
                "Rua C, 300",
                "Av. D, 400"
        );

        when(this.rideGateway.getById(eq(aRide.getId()))).thenReturn(Optional.of(aRide));
        when(this.rideGateway.save(any())).thenReturn(aRide.getId());
        final RideOutput anOutput = this.useCase.execute(aCommand);

        assertNotNull(anOutput);
        assertEquals(aRide.getId(), anOutput.id());
        assertEquals("Rua C, 300", anOutput.startAddress());
        assertEquals("Av. D, 400", anOutput.destinationAddress());
        assertEquals(RideStatus.CREATED, anOutput.status());
        verify(this.rideGateway, times(1)).save(eq(aRide));
        verify(this.driverNotifier, times(1)).notify(argThat((RideNotification notification) ->
                notification.rideId().equals(aRide.getId())
                        && notification.startAddress().equals("Rua C, 300")
                        && notification.destinationAddress().equals("Av. D, 400")
                        && notification.status().equals(RideStatus.CREATED.name())));
    }

    @Test
    void givenABlankUserId_whenCallsUpdateRide_thenThrowsException() {
        final UpdateRideCommand aCommand = new UpdateRideCommand(
                UUID.randomUUID().toString(),
                " ",
                "Rua C, 300",
                "Av. D, 400"
        );

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> this.useCase.execute(aCommand));

        assertEquals("'userId' should be not blank", exception.getMessage());
        verifyNoInteractions(this.rideGateway, this.driverNotifier);
    }

    @Test
    void givenANonExistentRide_whenCallsUpdateRide_thenThrowsNotFound() {
        final String expectedRideId = UUID.randomUUID().toString();
        final UpdateRideCommand aCommand = new UpdateRideCommand(
                expectedRideId,
                UUID.randomUUID().toString(),
                "Rua C, 300",
                "Av. D, 400"
        );

        when(this.rideGateway.getById(eq(expectedRideId))).thenReturn(Optional.empty());
        final NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> this.useCase.execute(aCommand));

        assertEquals("Ride with id %s was not found".formatted(expectedRideId), exception.getMessage());
        verify(this.rideGateway, times(0)).save(any());
        verify(this.driverNotifier, times(0)).notify(any());
    }

    @Test
    void givenADifferentUser_whenCallsUpdateRide_thenThrowsException() {
        final Ride aRide = Ride.newRide(UUID.randomUUID().toString(), "Rua A, 100", "Av. B, 200");
        final String anotherUserId = UUID.randomUUID().toString();
        final UpdateRideCommand aCommand = new UpdateRideCommand(
                aRide.getId(),
                anotherUserId,
                "Rua C, 300",
                "Av. D, 400"
        );

        when(this.rideGateway.getById(eq(aRide.getId()))).thenReturn(Optional.of(aRide));
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> this.useCase.execute(aCommand));

        assertEquals("User %s cannot edit ride %s".formatted(anotherUserId, aRide.getId()), exception.getMessage());
        verify(this.rideGateway, times(0)).save(any());
        verify(this.driverNotifier, times(0)).notify(any());
    }

    @Test
    void givenACompletedRide_whenCallsUpdateRide_thenThrowsStateConflict() {
        final Ride aRide = Ride.newRide(UUID.randomUUID().toString(), "Rua A, 100", "Av. B, 200");
        aRide.changeStatus(RideStatus.COMPLETED);
        final UpdateRideCommand aCommand = new UpdateRideCommand(
                aRide.getId(),
                aRide.getUserId(),
                "Rua C, 300",
                "Av. D, 400"
        );

        when(this.rideGateway.getById(eq(aRide.getId()))).thenReturn(Optional.of(aRide));
        final IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> this.useCase.execute(aCommand));

        assertEquals("Ride %s can no longer be edited".formatted(aRide.getId()), exception.getMessage());
        verify(this.rideGateway, times(0)).save(any());
        verify(this.driverNotifier, times(0)).notify(any());
    }

    @Test
    void givenACancelledRide_whenCallsUpdateRide_thenThrowsStateConflict() {
        final Ride aRide = Ride.newRide(UUID.randomUUID().toString(), "Rua A, 100", "Av. B, 200");
        aRide.changeStatus(RideStatus.CANCELLED);
        final UpdateRideCommand aCommand = new UpdateRideCommand(
                aRide.getId(),
                aRide.getUserId(),
                "Rua C, 300",
                "Av. D, 400"
        );

        when(this.rideGateway.getById(eq(aRide.getId()))).thenReturn(Optional.of(aRide));
        final IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> this.useCase.execute(aCommand));

        assertEquals("Ride %s can no longer be edited".formatted(aRide.getId()), exception.getMessage());
        verify(this.rideGateway, times(0)).save(any());
        verify(this.driverNotifier, times(0)).notify(any());
    }

    @Test
    void givenANotifierFailure_whenCallsUpdateRide_thenUpdateStillSucceeds() {
        final Ride aRide = Ride.newRide(UUID.randomUUID().toString(), "Rua A, 100", "Av. B, 200");
        final UpdateRideCommand aCommand = new UpdateRideCommand(
                aRide.getId(),
                aRide.getUserId(),
                "Rua C, 300",
                "Av. D, 400"
        );

        when(this.rideGateway.getById(eq(aRide.getId()))).thenReturn(Optional.of(aRide));
        when(this.rideGateway.save(any())).thenReturn(aRide.getId());
        doThrow(new IllegalStateException("WebSocket down")).when(this.driverNotifier).notify(any());
        final RideOutput anOutput = this.useCase.execute(aCommand);

        assertNotNull(anOutput);
        assertEquals("Rua C, 300", anOutput.startAddress());
        assertEquals("Av. D, 400", anOutput.destinationAddress());
        verify(this.rideGateway, times(1)).save(eq(aRide));
        verify(this.driverNotifier, times(1)).notify(any());
    }
}