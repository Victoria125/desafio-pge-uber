package com.vitoria.rideservice.infrastructure.broker.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaRideConfig {
    @Bean
    public NewTopic rideTopic() {
        return TopicBuilder
                .name("ride-topic")
                .build();
    }
}
