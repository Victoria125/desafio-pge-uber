package com.vitoria.rideservice.infrastructure.api.presenters;

import com.vitoria.rideservice.application.usecase.ride.retrieve.getbyid.RideOutput;
import com.vitoria.rideservice.application.usecase.ride.retrieve.list.ListRidesOutput;
import com.vitoria.rideservice.infrastructure.api.models.ListRidesResponse;
import com.vitoria.rideservice.infrastructure.api.models.RideResponse;

import java.util.function.Function;

public interface RidePresenter {
    Function<RideOutput, RideResponse> presenterSimple = output -> new RideResponse(
            output.id(),
            output.userId(),
            output.driverId(),
            output.startAddress(),
            output.destinationAddress(),
            output.status(),
            output.createdAt(),
            output.updatedAt()
    );

    Function<ListRidesOutput, ListRidesResponse> presenterList = output -> new ListRidesResponse(
            output.id(),
            output.userId(),
            output.driverId(),
            output.startAddress(),
            output.destinationAddress(),
            output.status(),
            output.createdAt()
    );
}
