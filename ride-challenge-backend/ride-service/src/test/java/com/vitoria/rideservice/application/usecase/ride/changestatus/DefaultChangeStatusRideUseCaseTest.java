package com.vitoria.rideservice.application.usecase.ride.changestatus;

import com.vitoria.rideservice.domain.RideGateway;
import com.vitoria.rideservice.domain.entities.Ride;
import com.vitoria.rideservice.domain.enums.RideStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultChangeStatusRideUseCaseTest {
    @InjectMocks
    private DefaultChangeStatusRideUseCase useCase;
    @Mock
    private RideGateway rideGateway;

    @Test
    void givenAValidCommand_whenCallsChangeStatus_thenSaveRideWithNewStatus() {

        final Ride aRide = Ride.newRide("user-1", "Rua A, 100", "Rua B, 200");
        final ChangeStatusCommand aCommand =
                new ChangeStatusCommand(aRide.getId(), RideStatus.COMPLETED);

        when(this.rideGateway.getById(aRide.getId())).thenReturn(Optional.of(aRide));
        when(this.rideGateway.save(any())).thenReturn(aRide.getId());

        this.useCase.execute(aCommand);

        final ArgumentCaptor<Ride> aCaptor = ArgumentCaptor.forClass(Ride.class);
        verify(this.rideGateway, times(1)).save(aCaptor.capture());
        assertEquals(aRide.getId(), aCaptor.getValue().getId());
        assertEquals(RideStatus.COMPLETED, aCaptor.getValue().getStatus());
    }

    @Test
    void givenAnInvalidId_whenCallsChangeStatus_thenThrowsNotFoundException() {

        final String expectedId = "invalid-id";
        final String expectedMessageError = "Ride with id %s was not found".formatted(expectedId);
        final ChangeStatusCommand aCommand =
                new ChangeStatusCommand(expectedId, RideStatus.CANCELLED);

        when(this.rideGateway.getById(expectedId)).thenReturn(Optional.empty());

        final NoSuchElementException exception =
                assertThrows(NoSuchElementException.class, () -> this.useCase.execute(aCommand));

        assertEquals(expectedMessageError, exception.getMessage());
        verify(this.rideGateway, times(0)).save(any());
    }

    @Test
    void givenAValidCommand_whenGatewayThrows_thenReturnException() {

        final String expectedMessageError = "Gateway Error";
        final Ride aRide = Ride.newRide("user-1", "Rua A, 100", "Rua B, 200");
        final ChangeStatusCommand aCommand =
                new ChangeStatusCommand(aRide.getId(), RideStatus.CANCELLED);

        when(this.rideGateway.getById(aRide.getId())).thenReturn(Optional.of(aRide));
        when(this.rideGateway.save(any())).thenThrow(new IllegalStateException(expectedMessageError));

        final IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> this.useCase.execute(aCommand));
        assertEquals(expectedMessageError, exception.getMessage());
    }
}
