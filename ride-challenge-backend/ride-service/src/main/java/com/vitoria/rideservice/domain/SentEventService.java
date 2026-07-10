package com.vitoria.rideservice.domain;

public interface SentEventService<T> {
    void sentEvent(T message);
}
