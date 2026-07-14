package com.vitoria.rideservice.application.usecase.ride.retrieve.list;

import com.vitoria.rideservice.domain.RideGateway;
import com.vitoria.rideservice.domain.entities.Ride;
import com.vitoria.rideservice.domain.enums.RideStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultListRidesUseCaseTest {
    @InjectMocks
    private DefaultListRidesUseCase useCase;
    @Mock
    private RideGateway rideGateway;

    @Test
    void givenAValidQuery_whenCallsListRides_thenReturnRides() {

        final Ride aCreatedRide = Ride.newRide("user-1", "Rua A, 100", "Rua B, 200");
        final Ride anAcceptedRide = Ride.newRide("user-2", "Rua C, 300", "Rua D, 400")
                .assignDriver("driver-1");

        when(this.rideGateway.getAll()).thenReturn(List.of(aCreatedRide, anAcceptedRide));
        final List<ListRidesOutput> anOutput = this.useCase.execute();

        assertNotNull(anOutput);
        assertEquals(2, anOutput.size());
        assertEquals(aCreatedRide.getId(), anOutput.get(0).id());
        assertEquals(aCreatedRide.getUserId(), anOutput.get(0).userId());
        assertEquals(RideStatus.CREATED, anOutput.get(0).status());
        assertNull(anOutput.get(0).driverId());
        assertEquals(anAcceptedRide.getId(), anOutput.get(1).id());
        assertEquals("driver-1", anOutput.get(1).driverId());
        assertEquals(RideStatus.IN_PROGRESS, anOutput.get(1).status());
        verify(this.rideGateway, times(1)).getAll();
    }

    @Test
    void givenAValidQuery_whenHasNoRides_thenReturnEmptyList() {

        when(this.rideGateway.getAll()).thenReturn(List.of());
        final List<ListRidesOutput> anOutput = this.useCase.execute();

        assertNotNull(anOutput);
        assertTrue(anOutput.isEmpty());
        verify(this.rideGateway, times(1)).getAll();
    }

    @Test
    void givenAValidQuery_whenGatewayThrows_thenReturnException() {

        final String expectedMessageError = "Gateway Error";

        when(this.rideGateway.getAll()).thenThrow(new IllegalStateException(expectedMessageError));

        final IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> this.useCase.execute());
        assertEquals(expectedMessageError, exception.getMessage());
        verify(this.rideGateway, times(1)).getAll();
    }
}
