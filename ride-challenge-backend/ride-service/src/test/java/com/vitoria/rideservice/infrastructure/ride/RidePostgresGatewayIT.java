package com.vitoria.rideservice.infrastructure.ride;

import com.vitoria.rideservice.domain.RideGateway;
import com.vitoria.rideservice.domain.entities.Ride;
import com.vitoria.rideservice.domain.enums.RideStatus;
import com.vitoria.rideservice.infrastructure.ride.persistence.RideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import(RidePostgresGateway.class)
class RidePostgresGatewayIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private RideGateway rideGateway;

    @Autowired
    private RideRepository rideRepository;

    @BeforeEach
    void cleanUp() {
        this.rideRepository.deleteAll();
    }

    @Test
    void givenAValidRide_whenCallsSave_thenPersistsOnPostgres() {

        final Ride aRide = Ride.newRide("user-1", "Rua A, 100", "Rua B, 200");

        final String anId = this.rideGateway.save(aRide);

        assertEquals(aRide.getId(), anId);
        assertEquals(1, this.rideRepository.count());
    }

    @Test
    void givenAPersistedRide_whenCallsGetById_thenReturnsRide() {

        final Ride aRide = Ride.newRide("user-1", "Rua A, 100", "Rua B, 200");
        this.rideGateway.save(aRide);

        final Optional<Ride> actual = this.rideGateway.getById(aRide.getId());

        assertTrue(actual.isPresent());
        assertEquals(aRide.getId(), actual.get().getId());
        assertEquals(aRide.getUserId(), actual.get().getUserId());
        assertEquals(aRide.getStartAddress(), actual.get().getStartAddress());
        assertEquals(aRide.getDestinationAddress(), actual.get().getDestinationAddress());
        assertEquals(RideStatus.CREATED, actual.get().getStatus());
        assertNull(actual.get().getDriverId());
    }

    @Test
    void givenAnAcceptedRide_whenCallsSave_thenPersistsDriverAndStatus() {

        final Ride aRide = Ride.newRide("user-1", "Rua A, 100", "Rua B, 200")
                .assignDriver("driver-1");
        this.rideGateway.save(aRide);

        final Optional<Ride> actual = this.rideGateway.getById(aRide.getId());

        assertTrue(actual.isPresent());
        assertEquals("driver-1", actual.get().getDriverId());
        assertEquals(RideStatus.IN_PROGRESS, actual.get().getStatus());
    }

    @Test
    void givenRidesInSeveralStates_whenCallsFindCreatedBefore_thenReturnsOnlyStaleCreatedRides() {

        final Instant now = Instant.now();
        final Instant limit = now.minusSeconds(120);

        final Ride aStaleCreated = Ride.with(
                UUID.randomUUID().toString(), "user-1", null,
                "Rua A, 100", "Rua B, 200",
                RideStatus.CREATED, now.minusSeconds(300), now.minusSeconds(300));

        final Ride aRecentCreated = Ride.with(
                UUID.randomUUID().toString(), "user-2", null,
                "Rua C, 300", "Rua D, 400",
                RideStatus.CREATED, now, now);

        final Ride aStaleInProgress = Ride.with(
                UUID.randomUUID().toString(), "user-3", "driver-1",
                "Rua E, 500", "Rua F, 600",
                RideStatus.IN_PROGRESS, now.minusSeconds(300), now.minusSeconds(200));

        this.rideGateway.save(aStaleCreated);
        this.rideGateway.save(aRecentCreated);
        this.rideGateway.save(aStaleInProgress);

        final List<Ride> actual = this.rideGateway.findCreatedBefore(limit);

        assertEquals(1, actual.size());
        assertEquals(aStaleCreated.getId(), actual.get(0).getId());
        assertEquals(RideStatus.CREATED, actual.get(0).getStatus());
    }
}
