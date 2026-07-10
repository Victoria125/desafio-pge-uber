package com.vitoria.rideservice.application.usecase.ride.retrieve.getstatus;

import com.vitoria.rideservice.domain.RideGateway;
import com.vitoria.rideservice.domain.RideStatusCache;
import com.vitoria.rideservice.domain.RideStatusData;
import com.vitoria.rideservice.domain.entities.Ride;
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
class DefaultGetRideStatusUseCaseTest {
    @InjectMocks
    private DefaultGetRideStatusUseCase useCase;
    @Mock
    private RideStatusCache rideStatusCache;
    @Mock
    private RideGateway rideGateway;

    @Test
    void givenACachedStatus_whenCallsGetRideStatus_thenReturnFromRedisWithoutDatabase() {
        
        final String expectedRideId = UUID.randomUUID().toString();
        final String expectedDriverId = UUID.randomUUID().toString();

        
        when(this.rideStatusCache.get(eq(expectedRideId)))
                .thenReturn(Optional.of(new RideStatusData(expectedRideId, "IN_PROGRESS", expectedDriverId)));
        final RideStatusOutput anOutput = this.useCase.execute(expectedRideId);

        
        assertEquals("IN_PROGRESS", anOutput.status());
        assertEquals(expectedDriverId, anOutput.driverId());
        assertEquals("redis", anOutput.source());
        verify(this.rideGateway, times(0)).getById(any());
    }

    @Test
    void givenACacheMiss_whenCallsGetRideStatus_thenFallbackToDatabase() {
        
        final Ride aRide = Ride.newRide(UUID.randomUUID().toString(), "Rua A, 100", "Av. B, 200");

        
        when(this.rideStatusCache.get(eq(aRide.getId()))).thenReturn(Optional.empty());
        when(this.rideGateway.getById(eq(aRide.getId()))).thenReturn(Optional.of(aRide));
        final RideStatusOutput anOutput = this.useCase.execute(aRide.getId());

        
        assertEquals("CREATED", anOutput.status());
        assertEquals("database", anOutput.source());
        verify(this.rideGateway, times(1)).getById(eq(aRide.getId()));
    }

    @Test
    void givenAnUnknownRide_whenCallsGetRideStatus_thenThrowsNotFound() {
        
        final String expectedRideId = "ride-inexistente";
        final String expectedMessageError = "Ride with id %s was not found".formatted(expectedRideId);

        
        when(this.rideStatusCache.get(eq(expectedRideId))).thenReturn(Optional.empty());
        when(this.rideGateway.getById(eq(expectedRideId))).thenReturn(Optional.empty());

        
        final NoSuchElementException exception =
                assertThrows(NoSuchElementException.class, () -> this.useCase.execute(expectedRideId));
        assertEquals(expectedMessageError, exception.getMessage());
    }
}
