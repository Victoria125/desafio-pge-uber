package com.vitoria.rideservice.infrastructure.websocket;

import com.vitoria.rideservice.domain.DriverNotifier;
import com.vitoria.rideservice.domain.RideNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class WebSocketDriverNotifier implements DriverNotifier {
    private static final Logger log = LoggerFactory.getLogger(WebSocketDriverNotifier.class);
    private static final String TOPIC = "/topic/rides";

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketDriverNotifier(final SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = Objects.requireNonNull(messagingTemplate);
    }

    @Override
    public void notify(final RideNotification notification) {
        log.info("Notificando motoristas via WebSocket :: corrida {} status {}",
                notification.rideId(), notification.status());
        this.messagingTemplate.convertAndSend(TOPIC, notification);
    }
}
