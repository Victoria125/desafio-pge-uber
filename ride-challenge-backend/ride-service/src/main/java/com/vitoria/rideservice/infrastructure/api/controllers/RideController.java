package com.vitoria.rideservice.infrastructure.api.controllers;

import com.vitoria.rideservice.application.usecase.ride.accept.AcceptRideCommand;
import com.vitoria.rideservice.application.usecase.ride.accept.AcceptRideUseCase;
import com.vitoria.rideservice.application.usecase.ride.create.CreateRideCommand;
import com.vitoria.rideservice.application.usecase.ride.create.CreateRideOutput;
import com.vitoria.rideservice.application.usecase.ride.create.CreateRideUseCase;
import com.vitoria.rideservice.application.usecase.ride.retrieve.getbyid.GetRideByIdUseCase;
import com.vitoria.rideservice.application.usecase.ride.retrieve.getstatus.GetRideStatusUseCase;
import com.vitoria.rideservice.application.usecase.ride.retrieve.getstatus.RideStatusOutput;
import com.vitoria.rideservice.application.usecase.ride.retrieve.list.ListRidesUseCase;
import com.vitoria.rideservice.application.usecase.ride.update.UpdateRideCommand;
import com.vitoria.rideservice.application.usecase.ride.update.UpdateRideUseCase;
import com.vitoria.rideservice.infrastructure.api.RideAPI;
import com.vitoria.rideservice.infrastructure.api.models.AcceptRideRequest;
import com.vitoria.rideservice.infrastructure.api.models.CreateRideRequest;
import com.vitoria.rideservice.infrastructure.api.models.CreateRideResponse;
import com.vitoria.rideservice.infrastructure.api.models.ListRidesResponse;
import com.vitoria.rideservice.infrastructure.api.models.RideResponse;
import com.vitoria.rideservice.infrastructure.api.models.RideStatusResponse;
import com.vitoria.rideservice.infrastructure.api.models.UpdateRideRequest;
import com.vitoria.rideservice.infrastructure.api.presenters.RidePresenter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@RestController
public class RideController implements RideAPI {
    private final CreateRideUseCase createRideUseCase;
    private final UpdateRideUseCase updateRideUseCase;
    private final AcceptRideUseCase acceptRideUseCase;
    private final GetRideByIdUseCase getRideByIdUseCase;
    private final GetRideStatusUseCase getRideStatusUseCase;
    private final ListRidesUseCase listRidesUseCase;

    public RideController(
            final CreateRideUseCase createRideUseCase,
            final UpdateRideUseCase updateRideUseCase,
            final AcceptRideUseCase acceptRideUseCase,
            final GetRideByIdUseCase getRideByIdUseCase,
            final GetRideStatusUseCase getRideStatusUseCase,
            final ListRidesUseCase listRidesUseCase
    ) {
        this.createRideUseCase = Objects.requireNonNull(createRideUseCase);
        this.updateRideUseCase = Objects.requireNonNull(updateRideUseCase);
        this.acceptRideUseCase = Objects.requireNonNull(acceptRideUseCase);
        this.getRideByIdUseCase = Objects.requireNonNull(getRideByIdUseCase);
        this.getRideStatusUseCase = Objects.requireNonNull(getRideStatusUseCase);
        this.listRidesUseCase = Objects.requireNonNull(listRidesUseCase);
    }

    @Override
    public ResponseEntity<CreateRideResponse> createRide(final CreateRideRequest aRequest) {
        final CreateRideCommand aCommand = new CreateRideCommand(
                aRequest.userId(),
                aRequest.startAddress(),
                aRequest.destinationAddress()
        );
        final CreateRideOutput anOutput = this.createRideUseCase.execute(aCommand);
        return ResponseEntity
                .created(URI.create("/rides/" + anOutput.id()))
                .body(new CreateRideResponse(anOutput.id()));
    }

    @Override
    public ResponseEntity<RideResponse> updateRide(final String id, final UpdateRideRequest aRequest) {
        final RideResponse response = RidePresenter.presenterSimple
                .apply(this.updateRideUseCase.execute(new UpdateRideCommand(
                        id,
                        aRequest.userId(),
                        aRequest.startAddress(),
                        aRequest.destinationAddress()
                )));
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<RideResponse> acceptRide(final String id, final AcceptRideRequest aRequest) {
        final RideResponse response = RidePresenter.presenterSimple
                .apply(this.acceptRideUseCase.execute(new AcceptRideCommand(id, aRequest.driverId())));
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<RideResponse> getRideById(final String id) {
        final RideResponse response = RidePresenter.presenterSimple
                .compose(this.getRideByIdUseCase::execute)
                .apply(id);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<RideStatusResponse> getRideStatus(final String id) {
        final RideStatusOutput anOutput = this.getRideStatusUseCase.execute(id);
        return ResponseEntity.ok(new RideStatusResponse(
                anOutput.rideId(),
                anOutput.status(),
                anOutput.driverId(),
                anOutput.source()
        ));
    }

    @Override
    public ResponseEntity<List<ListRidesResponse>> listRides() {
        final List<ListRidesResponse> response = this.listRidesUseCase.execute()
                .stream()
                .map(RidePresenter.presenterList)
                .toList();
        return ResponseEntity.ok(response);
    }
}