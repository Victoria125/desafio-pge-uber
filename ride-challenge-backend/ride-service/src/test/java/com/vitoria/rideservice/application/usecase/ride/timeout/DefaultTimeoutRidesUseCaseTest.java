package com.vitoria.rideservice.application.usecase.ride.timeout;

import com.vitoria.rideservice.domain.DriverNotifier;
import com.vitoria.rideservice.domain.RideGateway;
import com.vitoria.rideservice.domain.RideStatusCache;
import com.vitoria.rideservice.domain.entities.Ride;
import com.vitoria.rideservice.domain.enums.RideStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultTimeoutRidesUseCaseTest {
    @InjectMocks
    private DefaultTimeoutRidesUseCase useCase;
    @Mock
    private RideGateway rideGateway;
    @Mock
    private RideStatusCache rideStatusCache;
    @Mock
    private DriverNotifier driverNotifier;

    @Test
    void givenExpiredRides_whenCallsTimeoutRides_thenCancelAll() {
        
        final Ride ride1 = Ride.newRide(UUID.randomUUID().toString(), "Rua A, 100", "Av. B, 200");
        final Ride ride2 = Ride.newRide(UUID.randomUUID().toString(), "Rua C, 300", "Av. D, 400");
        final TimeoutRidesCommand aCommand = new TimeoutRidesCommand(Instant.now());

        
        when(this.rideGateway.findCreatedBefore(any())).thenReturn(List.of(ride1, ride2));
        when(this.rideGateway.save(any())).thenReturn(ride1.getId(), ride2.getId());
        final int cancelled = this.useCase.execute(aCommand);

        
        assertEquals(2, cancelled);
        assertEquals(RideStatus.CANCELLED, ride1.getStatus());
        assertEquals(RideStatus.CANCELLED, ride2.getStatus());
        verify(this.rideGateway, times(2)).save(any());
        verify(this.rideStatusCache, times(2)).put(any(), eq(RideStatus.CANCELLED), any());
        verify(this.driverNotifier, times(2)).notify(any());
    }

    @Test
    void givenNoExpiredRides_whenCallsTimeoutRides_thenReturnZero() {
        
        final TimeoutRidesCommand aCommand = new TimeoutRidesCommand(Instant.now());

        
        when(this.rideGateway.findCreatedBefore(any())).thenReturn(List.of());
        final int cancelled = this.useCase.execute(aCommand);

        
        assertEquals(0, cancelled);
        verify(this.rideGateway, times(0)).save(any());
        verify(this.driverNotifier, times(0)).notify(any());
    }

    @Test
    void givenOneRideFailing_whenCallsTimeoutRides_thenOthersStillProcessed() {
        
        final Ride ride1 = Ride.newRide(UUID.randomUUID().toString(), "Rua A, 100", "Av. B, 200");
        final Ride ride2 = Ride.newRide(UUID.randomUUID().toString(), "Rua C, 300", "Av. D, 400");
        final TimeoutRidesCommand aCommand = new TimeoutRidesCommand(Instant.now());

        
        when(this.rideGateway.findCreatedBefore(any())).thenReturn(List.of(ride1, ride2));
        when(this.rideGateway.save(any()))
                .thenThrow(new IllegalStateException("DB error"))
                .thenReturn(ride2.getId());
        final int cancelled = this.useCase.execute(aCommand);

        
        assertEquals(1, cancelled);
        verify(this.rideGateway, times(2)).save(any());
        verify(this.driverNotifier, times(1)).notify(any());
    }
}
