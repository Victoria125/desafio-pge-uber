package com.vitoria.rideservice.infrastructure.services.account.models;

public record AccountFeignResponse(
        String id,
        String name,
        String email,
        String type
) {
}
