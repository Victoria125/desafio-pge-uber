package com.vitoria.rideservice.infrastructure.broker.kafka.consumer;

import com.vitoria.rideservice.application.usecase.ride.create.RideCreatedMessage;
import com.vitoria.rideservice.domain.DriverNotifier;
import com.vitoria.rideservice.domain.RideNotification;
import com.vitoria.rideservice.domain.enums.RideStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class RideConsumer {
    private static final Logger log = LoggerFactory.getLogger(RideConsumer.class);

    private final DriverNotifier driverNotifier;

    public RideConsumer(final DriverNotifier driverNotifier) {
        this.driverNotifier = Objects.requireNonNull(driverNotifier);
    }

    @KafkaListener(topics = "ride-topic")
    public void consume(final RideCreatedMessage message) {
        log.info("Corrida recebida da fila :: {}", message);
        try {
            this.driverNotifier.notify(new RideNotification(
                    message.rideId(),
                    message.userId(),
                    null,
                    message.startAddress(),
                    message.destinationAddress(),
                    RideStatus.CREATED.name()
            ));
        } catch (RuntimeException e) {
            log.error("Falha ao notificar motoristas sobre a corrida {}: {}",
                    message.rideId(), e.getMessage(), e);
        }
    }
}
