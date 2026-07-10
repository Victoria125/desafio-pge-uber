package com.vitoria.rideservice.infrastructure.broker.kafka.service;

import com.vitoria.rideservice.application.usecase.ride.create.RideCreatedMessage;
import com.vitoria.rideservice.domain.SentEventService;
import com.vitoria.rideservice.infrastructure.broker.kafka.producer.RideProducer;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class SentEventServiceImpl implements SentEventService<RideCreatedMessage> {
    private final RideProducer rideProducer;

    public SentEventServiceImpl(final RideProducer rideProducer) {
        this.rideProducer = Objects.requireNonNull(rideProducer);
    }

    @Override
    public void sentEvent(final RideCreatedMessage message) {
        this.rideProducer.send(message);
    }
}
