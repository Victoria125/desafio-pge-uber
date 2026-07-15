package com.vitoria.rideservice.application.usecase.ride.cancel;

import com.vitoria.rideservice.application.usecase.ride.retrieve.getbyid.RideOutput;
import com.vitoria.rideservice.domain.DriverNotifier;
import com.vitoria.rideservice.domain.RideGateway;
import com.vitoria.rideservice.domain.RideNotification;
import com.vitoria.rideservice.domain.RideStatusCache;
import com.vitoria.rideservice.domain.entities.Ride;
import com.vitoria.rideservice.domain.enums.RideStatus;
import com.vitoria.rideservice.domain.exceptions.ForbiddenOperationException;
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
class DefaultCancelRideUseCaseTest {
    @InjectMocks
    private DefaultCancelRideUseCase useCase;
    @Mock
    private RideGateway rideGateway;
    @Mock
    private RideStatusCache rideStatusCache;
    @Mock
    private DriverNotifier driverNotifier;

    @Test
    void givenAValidOwnerAndOpenRide_whenCallsCancelRide_thenCancelsCachesAndNotifies() {
        final Ride aRide = Ride.newRide(UUID.randomUUID().toString(), "Rua A, 100", "Av. B, 200");
        final CancelRideCommand aCommand = new CancelRideCommand(aRide.getId(), aRide.getUserId());

        when(this.rideGateway.getById(eq(aRide.getId()))).thenReturn(Optional.of(aRide));
        when(this.rideGateway.save(any())).thenReturn(aRide.getId());
        final RideOutput anOutput = this.useCase.execute(aCommand);

        assertNotNull(anOutput);
        assertEquals(aRide.getId(), anOutput.id());
        assertEquals(RideStatus.CANCELLED, anOutput.status());
        verify(this.rideGateway, times(1)).save(eq(aRide));
        verify(this.rideStatusCache, times(1)).put(eq(aRide.getId()), eq(RideStatus.CANCELLED), any());
        verify(this.driverNotifier, times(1)).notify(argThat((RideNotification notification) ->
                notification.rideId().equals(aRide.getId())
                        && notification.status().equals(RideStatus.CANCELLED.name())));
    }

    @Test
    void givenABlankUserId_whenCallsCancelRide_thenThrowsException() {
        final CancelRideCommand aCommand = new CancelRideCommand(UUID.randomUUID().toString(), " ");

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> this.useCase.execute(aCommand));

        assertEquals("'userId' should be not blank", exception.getMessage());
        verifyNoInteractions(this.rideGateway, this.rideStatusCache, this.driverNotifier);
    }

    @Test
    void givenANonExistentRide_whenCallsCancelRide_thenThrowsNotFound() {
        final String expectedRideId = UUID.randomUUID().toString();
        final CancelRideCommand aCommand = new CancelRideCommand(expectedRideId, UUID.randomUUID().toString());

        when(this.rideGateway.getById(eq(expectedRideId))).thenReturn(Optional.empty());
        final NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> this.useCase.execute(aCommand));

        assertEquals("Ride with id %s was not found".formatted(expectedRideId), exception.getMessage());
        verify(this.rideGateway, times(0)).save(any());
        verifyNoInteractions(this.rideStatusCache, this.driverNotifier);
    }

    @Test
    void givenADifferentUser_whenCallsCancelRide_thenThrowsForbidden() {
        final Ride aRide = Ride.newRide(UUID.randomUUID().toString(), "Rua A, 100", "Av. B, 200");
        final String anotherUserId = UUID.randomUUID().toString();
        final CancelRideCommand aCommand = new CancelRideCommand(aRide.getId(), anotherUserId);

        when(this.rideGateway.getById(eq(aRide.getId()))).thenReturn(Optional.of(aRide));
        final ForbiddenOperationException exception = assertThrows(ForbiddenOperationException.class,
                () -> this.useCase.execute(aCommand));

        assertEquals("User %s cannot cancel ride %s".formatted(anotherUserId, aRide.getId()),
                exception.getMessage());
        verify(this.rideGateway, times(0)).save(any());
        verifyNoInteractions(this.rideStatusCache, this.driverNotifier);
    }

    @Test
    void givenACompletedRide_whenCallsCancelRide_thenThrowsStateConflict() {
        final Ride aRide = Ride.newRide(UUID.randomUUID().toString(), "Rua A, 100", "Av. B, 200");
        aRide.changeStatus(RideStatus.COMPLETED);
        final CancelRideCommand aCommand = new CancelRideCommand(aRide.getId(), aRide.getUserId());

        when(this.rideGateway.getById(eq(aRide.getId()))).thenReturn(Optional.of(aRide));
        final IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> this.useCase.execute(aCommand));

        assertEquals("Ride %s can no longer be cancelled".formatted(aRide.getId()), exception.getMessage());
        verify(this.rideGateway, times(0)).save(any());
        verifyNoInteractions(this.rideStatusCache, this.driverNotifier);
    }

    @Test
    void givenACacheFailure_whenCallsCancelRide_thenCancelStillSucceedsAndNotifies() {
        final Ride aRide = Ride.newRide(UUID.randomUUID().toString(), "Rua A, 100", "Av. B, 200");
        final CancelRideCommand aCommand = new CancelRideCommand(aRide.getId(), aRide.getUserId());

        when(this.rideGateway.getById(eq(aRide.getId()))).thenReturn(Optional.of(aRide));
        when(this.rideGateway.save(any())).thenReturn(aRide.getId());
        doThrow(new IllegalStateException("Redis down")).when(this.rideStatusCache).put(any(), any(), any());
        final RideOutput anOutput = this.useCase.execute(aCommand);

        assertEquals(RideStatus.CANCELLED, anOutput.status());
        verify(this.driverNotifier, times(1)).notify(any());
    }

    @Test
    void givenANotifierFailure_whenCallsCancelRide_thenCancelStillSucceeds() {
        final Ride aRide = Ride.newRide(UUID.randomUUID().toString(), "Rua A, 100", "Av. B, 200");
        final CancelRideCommand aCommand = new CancelRideCommand(aRide.getId(), aRide.getUserId());

        when(this.rideGateway.getById(eq(aRide.getId()))).thenReturn(Optional.of(aRide));
        when(this.rideGateway.save(any())).thenReturn(aRide.getId());
        doThrow(new IllegalStateException("WebSocket down")).when(this.driverNotifier).notify(any());
        final RideOutput anOutput = this.useCase.execute(aCommand);

        assertEquals(RideStatus.CANCELLED, anOutput.status());
        verify(this.rideGateway, times(1)).save(eq(aRide));
        verify(this.driverNotifier, times(1)).notify(any());
    }
}
