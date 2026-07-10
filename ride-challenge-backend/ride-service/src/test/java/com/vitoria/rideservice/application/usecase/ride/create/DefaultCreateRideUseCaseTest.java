package com.vitoria.rideservice.application.usecase.ride.create;

import com.vitoria.rideservice.domain.AccountClient;
import com.vitoria.rideservice.domain.AccountData;
import com.vitoria.rideservice.domain.RideGateway;
import com.vitoria.rideservice.domain.SentEventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultCreateRideUseCaseTest {
    @InjectMocks
    private DefaultCreateRideUseCase useCase;
    @Mock
    private RideGateway rideGateway;
    @Mock
    private AccountClient accountClient;
    @Mock
    private SentEventService<RideCreatedMessage> sentEventService;

    @Test
    void givenAValidParams_whenCallsCreateRide_thenReturnRideId() {
        
        final String expectedUserId = UUID.randomUUID().toString();
        final String expectedRideId = UUID.randomUUID().toString();
        final CreateRideCommand aCommand =
                new CreateRideCommand(expectedUserId, "Rua A, 100", "Av. B, 200");

        
        when(this.accountClient.getById(eq(expectedUserId)))
                .thenReturn(Optional.of(new AccountData(expectedUserId, "Maria", "CLIENT")));
        when(this.rideGateway.save(any())).thenReturn(expectedRideId);
        doNothing().when(this.sentEventService).sentEvent(any());
        final CreateRideOutput anOutput = this.useCase.execute(aCommand);

        
        assertNotNull(anOutput);
        assertEquals(expectedRideId, anOutput.id());
        verify(this.accountClient, times(1)).getById(eq(expectedUserId));
        verify(this.rideGateway, times(1)).save(any());
        verify(this.sentEventService, times(1)).sentEvent(any());
    }

    @Test
    void givenANonExistentUser_whenCallsCreateRide_thenThrowsException() {
        
        final String expectedUserId = UUID.randomUUID().toString();
        final String expectedMessageError = "User with id %s does not exist".formatted(expectedUserId);
        final CreateRideCommand aCommand =
                new CreateRideCommand(expectedUserId, "Rua A, 100", "Av. B, 200");

        
        when(this.accountClient.getById(eq(expectedUserId))).thenReturn(Optional.empty());

        
        final IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> this.useCase.execute(aCommand));
        assertEquals(expectedMessageError, exception.getMessage());
        verify(this.rideGateway, times(0)).save(any());
        verify(this.sentEventService, times(0)).sentEvent(any());
    }

    @Test
    void givenAnInvalidBlankStartAddress_whenCallsCreateRide_thenThrowsException() {
        
        final String expectedMessageError = "'startAddress' should be not blank";
        final CreateRideCommand aCommand =
                new CreateRideCommand(UUID.randomUUID().toString(), "  ", "Av. B, 200");

        
        final IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> this.useCase.execute(aCommand));

        
        assertEquals(expectedMessageError, exception.getMessage());
        verify(this.accountClient, times(0)).getById(any());
        verify(this.rideGateway, times(0)).save(any());
        verify(this.sentEventService, times(0)).sentEvent(any());
    }

    @Test
    void givenAnInvalidNullDestinationAddress_whenCallsCreateRide_thenThrowsException() {
        
        final String expectedMessageError = "'destinationAddress' should be not null";
        final CreateRideCommand aCommand =
                new CreateRideCommand(UUID.randomUUID().toString(), "Rua A, 100", null);

        
        final IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> this.useCase.execute(aCommand));

        
        assertEquals(expectedMessageError, exception.getMessage());
        verify(this.accountClient, times(0)).getById(any());
        verify(this.rideGateway, times(0)).save(any());
        verify(this.sentEventService, times(0)).sentEvent(any());
    }

    @Test
    void givenAQueueFailure_whenCallsCreateRide_thenLogsAndPropagates() {
        
        final String expectedUserId = UUID.randomUUID().toString();
        final String expectedMessageError = "Queue communication error";
        final CreateRideCommand aCommand =
                new CreateRideCommand(expectedUserId, "Rua A, 100", "Av. B, 200");

        
        when(this.accountClient.getById(eq(expectedUserId)))
                .thenReturn(Optional.of(new AccountData(expectedUserId, "Maria", "CLIENT")));
        when(this.rideGateway.save(any())).thenReturn(UUID.randomUUID().toString());
        doThrow(new IllegalStateException(expectedMessageError)).when(this.sentEventService).sentEvent(any());

        
        final IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> this.useCase.execute(aCommand));
        assertEquals(expectedMessageError, exception.getMessage());
        verify(this.rideGateway, times(1)).save(any());
        verify(this.sentEventService, times(1)).sentEvent(any());
    }

    @Test
    void givenAGatewayFailure_whenCallsCreateRide_thenPropagatesException() {
        
        final String expectedUserId = UUID.randomUUID().toString();
        final String expectedMessageError = "Gateway Error";
        final CreateRideCommand aCommand =
                new CreateRideCommand(expectedUserId, "Rua A, 100", "Av. B, 200");

        
        when(this.accountClient.getById(eq(expectedUserId)))
                .thenReturn(Optional.of(new AccountData(expectedUserId, "Maria", "CLIENT")));
        when(this.rideGateway.save(any())).thenThrow(new IllegalStateException(expectedMessageError));

        
        final IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> this.useCase.execute(aCommand));
        assertEquals(expectedMessageError, exception.getMessage());
        verify(this.sentEventService, times(0)).sentEvent(any());
    }
}
