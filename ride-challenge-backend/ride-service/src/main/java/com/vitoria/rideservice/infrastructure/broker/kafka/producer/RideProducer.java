package com.vitoria.rideservice.infrastructure.broker.kafka.producer;

import com.vitoria.rideservice.application.usecase.ride.create.RideCreatedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class RideProducer {
    private static final Logger log = LoggerFactory.getLogger(RideProducer.class);
    private final KafkaTemplate<String, RideCreatedMessage> kafkaTemplate;

    public RideProducer(final KafkaTemplate<String, RideCreatedMessage> kafkaTemplate) {
        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate);
    }

    public void send(final RideCreatedMessage data) {
        log.info("Publicando corrida na fila :: {}", data);
        final Message<RideCreatedMessage> message = MessageBuilder
                .withPayload(data)
                .setHeader(KafkaHeaders.TOPIC, "ride-topic")
                .build();
        this.kafkaTemplate.send(message)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error("Falha de comunicacao com a fila ao publicar corrida {}: {}",
                                data.rideId(), exception.getMessage(), exception);
                    } else {
                        log.info("Corrida {} publicada no topico ride-topic", data.rideId());
                    }
                });
    }
}
