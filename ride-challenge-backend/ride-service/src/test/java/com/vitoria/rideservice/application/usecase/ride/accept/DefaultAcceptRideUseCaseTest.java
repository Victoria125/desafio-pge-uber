package com.vitoria.rideservice.application.usecase.ride.accept;

import com.vitoria.rideservice.application.usecase.ride.retrieve.getbyid.RideOutput;
import com.vitoria.rideservice.domain.*;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultAcceptRideUseCaseTest {
    @InjectMocks
    private DefaultAcceptRideUseCase useCase;
    @Mock
    private RideGateway rideGateway;
    @Mock
    private AccountClient accountClient;
    @Mock
    private RideStatusCache rideStatusCache;
    @Mock
    private DriverNotifier driverNotifier;

    @Test
    void givenAValidDriverAndCreatedRide_whenCallsAcceptRide_thenBindDriverAndSetInProgress() {
        
        final String expectedDriverId = UUID.randomUUID().toString();
        final Ride aRide = Ride.newRide(UUID.randomUUID().toString(), "Rua A, 100", "Av. B, 200");
        final AcceptRideCommand aCommand = new AcceptRideCommand(aRide.getId(), expectedDriverId);

        
        when(this.accountClient.getById(eq(expectedDriverId)))
                .thenReturn(Optional.of(new AccountData(expectedDriverId, "Joao", "DRIVER")));
        when(this.rideGateway.getById(eq(aRide.getId()))).thenReturn(Optional.of(aRide));
        when(this.rideGateway.save(any())).thenReturn(aRide.getId());
        final RideOutput anOutput = this.useCase.execute(aCommand);

        
        assertNotNull(anOutput);
        assertEquals(expectedDriverId, anOutput.driverId());
        assertEquals(RideStatus.IN_PROGRESS, anOutput.status());
        verify(this.rideGateway, times(1)).save(any());
        verify(this.rideStatusCache, times(1))
                .put(eq(aRide.getId()), eq(RideStatus.IN_PROGRESS), eq(expectedDriverId));
        verify(this.driverNotifier, times(1)).notify(any());
    }

    @Test
    void givenANonExistentDriver_whenCallsAcceptRide_thenThrowsException() {
        
        final String expectedDriverId = UUID.randomUUID().toString();
        final String expectedMessageError = "Driver with id %s does not exist".formatted(expectedDriverId);
        final AcceptRideCommand aCommand = new AcceptRideCommand(UUID.randomUUID().toString(), expectedDriverId);

        
        when(this.accountClient.getById(eq(expectedDriverId))).thenReturn(Optional.empty());

        
        final IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> this.useCase.execute(aCommand));
        assertEquals(expectedMessageError, exception.getMessage());
        verify(this.rideGateway, times(0)).save(any());
        verify(this.driverNotifier, times(0)).notify(any());
    }

    @Test
    void givenAClientAccount_whenCallsAcceptRide_thenThrowsException() {
        
        final String expectedDriverId = UUID.randomUUID().toString();
        final String expectedMessageError = "Account %s is not a driver".formatted(expectedDriverId);
        final AcceptRideCommand aCommand = new AcceptRideCommand(UUID.randomUUID().toString(), expectedDriverId);

        
        when(this.accountClient.getById(eq(expectedDriverId)))
                .thenReturn(Optional.of(new AccountData(expectedDriverId, "Maria", "CLIENT")));

        
        final IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> this.useCase.execute(aCommand));
        assertEquals(expectedMessageError, exception.getMessage());
        verify(this.rideGateway, times(0)).save(any());
    }

    @Test
    void givenANonExistentRide_whenCallsAcceptRide_thenThrowsNotFound() {
        
        final String expectedDriverId = UUID.randomUUID().toString();
        final String expectedRideId = "ride-inexistente";
        final String expectedMessageError = "Ride with id %s was not found".formatted(expectedRideId);
        final AcceptRideCommand aCommand = new AcceptRideCommand(expectedRideId, expectedDriverId);

        
        when(this.accountClient.getById(eq(expectedDriverId)))
                .thenReturn(Optional.of(new AccountData(expectedDriverId, "Joao", "DRIVER")));
        when(this.rideGateway.getById(eq(expectedRideId))).thenReturn(Optional.empty());

        
        final NoSuchElementException exception =
                assertThrows(NoSuchElementException.class, () -> this.useCase.execute(aCommand));
        assertEquals(expectedMessageError, exception.getMessage());
        verify(this.rideGateway, times(0)).save(any());
    }

    @Test
    void givenAnAlreadyAcceptedRide_whenCallsAcceptRide_thenThrowsStateConflict() {
        
        final String expectedDriverId = UUID.randomUUID().toString();
        final Ride aRide = Ride.newRide(UUID.randomUUID().toString(), "Rua A, 100", "Av. B, 200")
                .assignDriver(UUID.randomUUID().toString());
        final String expectedMessageError = "Ride %s was already accepted or finished".formatted(aRide.getId());
        final AcceptRideCommand aCommand = new AcceptRideCommand(aRide.getId(), expectedDriverId);

        
        when(this.accountClient.getById(eq(expectedDriverId)))
                .thenReturn(Optional.of(new AccountData(expectedDriverId, "Joao", "DRIVER")));
        when(this.rideGateway.getById(eq(aRide.getId()))).thenReturn(Optional.of(aRide));

        
        final IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> this.useCase.execute(aCommand));
        assertEquals(expectedMessageError, exception.getMessage());
        verify(this.rideGateway, times(0)).save(any());
        verify(this.driverNotifier, times(0)).notify(any());
    }

    @Test
    void givenACacheFailure_whenCallsAcceptRide_thenAcceptStillSucceeds() {
        
        final String expectedDriverId = UUID.randomUUID().toString();
        final Ride aRide = Ride.newRide(UUID.randomUUID().toString(), "Rua A, 100", "Av. B, 200");
        final AcceptRideCommand aCommand = new AcceptRideCommand(aRide.getId(), expectedDriverId);

        
        when(this.accountClient.getById(eq(expectedDriverId)))
                .thenReturn(Optional.of(new AccountData(expectedDriverId, "Joao", "DRIVER")));
        when(this.rideGateway.getById(eq(aRide.getId()))).thenReturn(Optional.of(aRide));
        when(this.rideGateway.save(any())).thenReturn(aRide.getId());
        doThrow(new IllegalStateException("Redis down")).when(this.rideStatusCache).put(any(), any(), any());
        final RideOutput anOutput = this.useCase.execute(aCommand);

        
        assertNotNull(anOutput);
        assertEquals(RideStatus.IN_PROGRESS, anOutput.status());
        verify(this.rideGateway, times(1)).save(any());
        verify(this.driverNotifier, times(1)).notify(any());
    }
}
