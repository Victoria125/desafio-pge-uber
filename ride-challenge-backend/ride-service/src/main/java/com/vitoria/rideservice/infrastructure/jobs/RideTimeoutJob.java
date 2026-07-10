package com.vitoria.rideservice.infrastructure.jobs;

import com.vitoria.rideservice.application.usecase.ride.timeout.TimeoutRidesCommand;
import com.vitoria.rideservice.application.usecase.ride.timeout.TimeoutRidesUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;

@Component
public class RideTimeoutJob {
    private static final Logger log = LoggerFactory.getLogger(RideTimeoutJob.class);

    private final TimeoutRidesUseCase timeoutRidesUseCase;
    private final long timeoutSeconds;

    public RideTimeoutJob(
            final TimeoutRidesUseCase timeoutRidesUseCase,
            @Value("${ride.timeout.seconds:120}") final long timeoutSeconds
    ) {
        this.timeoutRidesUseCase = Objects.requireNonNull(timeoutRidesUseCase);
        this.timeoutSeconds = timeoutSeconds;
    }

    @Scheduled(fixedDelayString = "${ride.timeout.check-interval-ms:30000}")
    public void execute() {
        final Instant limit = Instant.now().minusSeconds(this.timeoutSeconds);
        final int cancelled = this.timeoutRidesUseCase.execute(new TimeoutRidesCommand(limit));
        if (cancelled > 0) {
            log.info("Job de timeout: {} corrida(s) cancelada(s) por falta de aceite em {}s",
                    cancelled, this.timeoutSeconds);
        }
    }
}
