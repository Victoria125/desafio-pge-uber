package com.vitoria.rideservice.domain.exceptions;

public class ForbiddenOperationException extends RuntimeException {
    public ForbiddenOperationException(final String message) {
        super(message);
    }
}
