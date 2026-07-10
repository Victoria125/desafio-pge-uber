package com.vitoria.rideservice.application.usecase.ride.retrieve.getbyid;

import com.vitoria.rideservice.domain.RideGateway;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultGetRideByIdUseCaseTest {
    @InjectMocks
    private DefaultGetRideByIdUseCase useCase;
    @Mock
    private RideGateway rideGateway;

    @Test
    void givenAValidId_whenCallsGetRideById_thenReturnRide() {
        
        final Ride aRide = Ride.newRide(UUID.randomUUID().toString(), "Rua A, 100", "Av. B, 200");
        final String expectedId = aRide.getId();

        
        when(this.rideGateway.getById(eq(expectedId))).thenReturn(Optional.of(aRide));
        final RideOutput anOutput = this.useCase.execute(expectedId);

        
        assertNotNull(anOutput);
        assertEquals(expectedId, anOutput.id());
        assertEquals(aRide.getUserId(), anOutput.userId());
        assertEquals(aRide.getStartAddress(), anOutput.startAddress());
        assertEquals(aRide.getDestinationAddress(), anOutput.destinationAddress());
        assertEquals(RideStatus.CREATED, anOutput.status());
        verify(this.rideGateway, times(1)).getById(eq(expectedId));
    }

    @Test
    void givenAnInvalidId_whenCallsGetRideById_thenThrowsNotFound() {
        
        final String expectedId = "invalid-id";
        final String expectedMessageError = "Ride with id invalid-id was not found";

        
        when(this.rideGateway.getById(eq(expectedId))).thenReturn(Optional.empty());

        
        final NoSuchElementException exception =
                assertThrows(NoSuchElementException.class, () -> this.useCase.execute(expectedId));
        assertEquals(expectedMessageError, exception.getMessage());
        verify(this.rideGateway, times(1)).getById(eq(expectedId));
    }
}
