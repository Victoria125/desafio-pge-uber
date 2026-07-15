package com.vitoria.rideservice.infrastructure.api.controllers;

import com.vitoria.rideservice.application.usecase.ride.accept.AcceptRideCommand;
import com.vitoria.rideservice.application.usecase.ride.accept.AcceptRideUseCase;
import com.vitoria.rideservice.application.usecase.ride.cancel.CancelRideCommand;
import com.vitoria.rideservice.application.usecase.ride.cancel.CancelRideUseCase;
import com.vitoria.rideservice.application.usecase.ride.create.CreateRideCommand;
import com.vitoria.rideservice.application.usecase.ride.create.CreateRideOutput;
import com.vitoria.rideservice.application.usecase.ride.create.CreateRideUseCase;
import com.vitoria.rideservice.application.usecase.ride.retrieve.getbyid.GetRideByIdUseCase;
import com.vitoria.rideservice.application.usecase.ride.retrieve.getbyid.RideOutput;
import com.vitoria.rideservice.application.usecase.ride.retrieve.getstatus.GetRideStatusUseCase;
import com.vitoria.rideservice.application.usecase.ride.retrieve.list.ListRidesUseCase;
import com.vitoria.rideservice.application.usecase.ride.update.UpdateRideUseCase;
import com.vitoria.rideservice.domain.enums.RideStatus;
import com.vitoria.rideservice.domain.exceptions.ForbiddenOperationException;
import com.vitoria.rideservice.infrastructure.api.models.AcceptRideRequest;
import com.vitoria.rideservice.infrastructure.api.models.CreateRideRequest;
import com.vitoria.rideservice.infrastructure.api.models.CreateRideResponse;
import com.vitoria.rideservice.infrastructure.api.models.RideResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideControllerTest {
    @InjectMocks
    private RideController controller;
    @Mock
    private CreateRideUseCase createRideUseCase;
    @Mock
    private UpdateRideUseCase updateRideUseCase;
    @Mock
    private AcceptRideUseCase acceptRideUseCase;
    @Mock
    private CancelRideUseCase cancelRideUseCase;
    @Mock
    private GetRideByIdUseCase getRideByIdUseCase;
    @Mock
    private GetRideStatusUseCase getRideStatusUseCase;
    @Mock
    private ListRidesUseCase listRidesUseCase;

    @Test
    void givenAClientCreatingItsOwnRide_whenCallsCreateRide_thenDelegatesToUseCase() {
        final String userId = UUID.randomUUID().toString();
        final CreateRideRequest aRequest = new CreateRideRequest(userId, "Rua A, 100", "Av. B, 200");
        final String expectedRideId = UUID.randomUUID().toString();
        when(this.createRideUseCase.execute(any())).thenReturn(new CreateRideOutput(expectedRideId));

        final ResponseEntity<CreateRideResponse> response =
                this.controller.createRide(userId, "CLIENT", aRequest);

        assertEquals(expectedRideId, response.getBody().id());
        final ArgumentCaptor<CreateRideCommand> captor = ArgumentCaptor.forClass(CreateRideCommand.class);
        verify(this.createRideUseCase, times(1)).execute(captor.capture());
        assertEquals(userId, captor.getValue().userId());
    }

    @Test
    void givenADriverIdentity_whenCallsCreateRide_thenThrowsForbidden() {
        final String userId = UUID.randomUUID().toString();
        final CreateRideRequest aRequest = new CreateRideRequest(userId, "Rua A, 100", "Av. B, 200");

        final ForbiddenOperationException exception = assertThrows(
                ForbiddenOperationException.class,
                () -> this.controller.createRide(userId, "DRIVER", aRequest));

        assertEquals("Only clients can create rides", exception.getMessage());
        verifyNoInteractions(this.createRideUseCase);
    }

    @Test
    void givenAUserIdDifferentFromAuthenticated_whenCallsCreateRide_thenThrowsForbidden() {
        final CreateRideRequest aRequest =
                new CreateRideRequest(UUID.randomUUID().toString(), "Rua A, 100", "Av. B, 200");

        final ForbiddenOperationException exception = assertThrows(
                ForbiddenOperationException.class,
                () -> this.controller.createRide(UUID.randomUUID().toString(), "CLIENT", aRequest));

        assertEquals("'userId' must match the authenticated user", exception.getMessage());
        verifyNoInteractions(this.createRideUseCase);
    }

    @Test
    void givenADriverAcceptingAsItself_whenCallsAcceptRide_thenDelegatesToUseCase() {
        final String rideId = UUID.randomUUID().toString();
        final String driverId = UUID.randomUUID().toString();
        when(this.acceptRideUseCase.execute(any())).thenReturn(rideOutput(rideId, driverId));

        final ResponseEntity<RideResponse> response =
                this.controller.acceptRide(rideId, driverId, "DRIVER", new AcceptRideRequest(driverId));

        assertEquals(driverId, response.getBody().driverId());
        final ArgumentCaptor<AcceptRideCommand> captor = ArgumentCaptor.forClass(AcceptRideCommand.class);
        verify(this.acceptRideUseCase, times(1)).execute(captor.capture());
        assertEquals(driverId, captor.getValue().driverId());
    }

    @Test
    void givenAClientIdentity_whenCallsAcceptRide_thenThrowsForbidden() {
        final String driverId = UUID.randomUUID().toString();

        final ForbiddenOperationException exception = assertThrows(
                ForbiddenOperationException.class,
                () -> this.controller.acceptRide(
                        UUID.randomUUID().toString(), driverId, "CLIENT", new AcceptRideRequest(driverId)));

        assertEquals("Only drivers can accept rides", exception.getMessage());
        verifyNoInteractions(this.acceptRideUseCase);
    }

    @Test
    void givenADriverIdDifferentFromAuthenticated_whenCallsAcceptRide_thenThrowsForbidden() {
        final ForbiddenOperationException exception = assertThrows(
                ForbiddenOperationException.class,
                () -> this.controller.acceptRide(
                        UUID.randomUUID().toString(),
                        UUID.randomUUID().toString(),
                        "DRIVER",
                        new AcceptRideRequest(UUID.randomUUID().toString())));

        assertEquals("'driverId' must match the authenticated user", exception.getMessage());
        verifyNoInteractions(this.acceptRideUseCase);
    }

    @Test
    void givenAClientCancellingItsOwnRide_whenCallsCancelRide_thenDelegatesToUseCase() {
        final String rideId = UUID.randomUUID().toString();
        final String userId = UUID.randomUUID().toString();
        when(this.cancelRideUseCase.execute(any())).thenReturn(new RideOutput(
                rideId,
                userId,
                null,
                "Rua A, 100",
                "Av. B, 200",
                RideStatus.CANCELLED,
                Instant.now(),
                Instant.now()
        ));

        final ResponseEntity<RideResponse> response = this.controller.cancelRide(rideId, userId, "CLIENT");

        assertEquals(RideStatus.CANCELLED, response.getBody().status());
        final ArgumentCaptor<CancelRideCommand> captor = ArgumentCaptor.forClass(CancelRideCommand.class);
        verify(this.cancelRideUseCase, times(1)).execute(captor.capture());
        assertEquals(rideId, captor.getValue().rideId());
        assertEquals(userId, captor.getValue().userId());
    }

    @Test
    void givenADriverIdentity_whenCallsCancelRide_thenThrowsForbidden() {
        final ForbiddenOperationException exception = assertThrows(
                ForbiddenOperationException.class,
                () -> this.controller.cancelRide(
                        UUID.randomUUID().toString(), UUID.randomUUID().toString(), "DRIVER"));

        assertEquals("Only clients can cancel rides", exception.getMessage());
        verifyNoInteractions(this.cancelRideUseCase);
    }

    private static RideOutput rideOutput(final String rideId, final String driverId) {
        return new RideOutput(
                rideId,
                UUID.randomUUID().toString(),
                driverId,
                "Rua A, 100",
                "Av. B, 200",
                RideStatus.IN_PROGRESS,
                Instant.now(),
                Instant.now()
        );
    }
}
