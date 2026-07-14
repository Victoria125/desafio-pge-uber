package com.vitoria.rideservice.infrastructure.api;

import com.vitoria.rideservice.infrastructure.api.models.AcceptRideRequest;
import com.vitoria.rideservice.infrastructure.api.models.CreateRideRequest;
import com.vitoria.rideservice.infrastructure.api.models.CreateRideResponse;
import com.vitoria.rideservice.infrastructure.api.models.ListRidesResponse;
import com.vitoria.rideservice.infrastructure.api.models.RideResponse;
import com.vitoria.rideservice.infrastructure.api.models.RideStatusResponse;
import com.vitoria.rideservice.infrastructure.api.models.UpdateRideRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(value = "/rides")
public interface RideAPI {
    String USER_ID_HEADER = "X-User-Id";
    String USER_TYPE_HEADER = "X-User-Type";

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CreateRideResponse> createRide(
            @RequestHeader(name = USER_ID_HEADER) String authenticatedUserId,
            @RequestHeader(name = USER_TYPE_HEADER) String authenticatedUserType,
            @Valid @RequestBody CreateRideRequest aRequest);

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<RideResponse> updateRide(
            @PathVariable(name = "id") String id,
            @RequestHeader(name = USER_ID_HEADER) String authenticatedUserId,
            @RequestHeader(name = USER_TYPE_HEADER) String authenticatedUserType,
            @Valid @RequestBody UpdateRideRequest aRequest);

    @PostMapping(
            value = "/{id}/accept",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<RideResponse> acceptRide(
            @PathVariable(name = "id") String id,
            @RequestHeader(name = USER_ID_HEADER) String authenticatedUserId,
            @RequestHeader(name = USER_TYPE_HEADER) String authenticatedUserType,
            @Valid @RequestBody AcceptRideRequest aRequest);

    @GetMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<RideResponse> getRideById(@PathVariable(name = "id") String id);

    @GetMapping(
            value = "/{id}/status",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<RideStatusResponse> getRideStatus(@PathVariable(name = "id") String id);

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<ListRidesResponse>> listRides();
}