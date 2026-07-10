package com.vitoria.rideservice.infrastructure.configuration.beans.ride;

import com.vitoria.rideservice.application.usecase.ride.accept.AcceptRideUseCase;
import com.vitoria.rideservice.application.usecase.ride.accept.DefaultAcceptRideUseCase;
import com.vitoria.rideservice.application.usecase.ride.changestatus.ChangeStatusRideUseCase;
import com.vitoria.rideservice.application.usecase.ride.changestatus.DefaultChangeStatusRideUseCase;
import com.vitoria.rideservice.application.usecase.ride.create.CreateRideUseCase;
import com.vitoria.rideservice.application.usecase.ride.create.DefaultCreateRideUseCase;
import com.vitoria.rideservice.application.usecase.ride.create.RideCreatedMessage;
import com.vitoria.rideservice.application.usecase.ride.retrieve.getbyid.DefaultGetRideByIdUseCase;
import com.vitoria.rideservice.application.usecase.ride.retrieve.getbyid.GetRideByIdUseCase;
import com.vitoria.rideservice.application.usecase.ride.retrieve.getstatus.DefaultGetRideStatusUseCase;
import com.vitoria.rideservice.application.usecase.ride.retrieve.getstatus.GetRideStatusUseCase;
import com.vitoria.rideservice.application.usecase.ride.retrieve.list.DefaultListRidesUseCase;
import com.vitoria.rideservice.application.usecase.ride.retrieve.list.ListRidesUseCase;
import com.vitoria.rideservice.application.usecase.ride.timeout.DefaultTimeoutRidesUseCase;
import com.vitoria.rideservice.application.usecase.ride.timeout.TimeoutRidesUseCase;
import com.vitoria.rideservice.application.usecase.ride.update.DefaultUpdateRideUseCase;
import com.vitoria.rideservice.application.usecase.ride.update.UpdateRideUseCase;
import com.vitoria.rideservice.domain.AccountClient;
import com.vitoria.rideservice.domain.DriverNotifier;
import com.vitoria.rideservice.domain.RideGateway;
import com.vitoria.rideservice.domain.RideStatusCache;
import com.vitoria.rideservice.domain.SentEventService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
public class RideBeansConfig {
    private final RideGateway rideGateway;
    private final AccountClient accountClient;
    private final SentEventService<RideCreatedMessage> sentEventService;
    private final RideStatusCache rideStatusCache;
    private final DriverNotifier driverNotifier;

    public RideBeansConfig(
            final RideGateway rideGateway,
            final AccountClient accountClient,
            final SentEventService<RideCreatedMessage> sentEventService,
            final RideStatusCache rideStatusCache,
            final DriverNotifier driverNotifier
    ) {
        this.rideGateway = Objects.requireNonNull(rideGateway);
        this.accountClient = Objects.requireNonNull(accountClient);
        this.sentEventService = Objects.requireNonNull(sentEventService);
        this.rideStatusCache = Objects.requireNonNull(rideStatusCache);
        this.driverNotifier = Objects.requireNonNull(driverNotifier);
    }

    @Bean
    public CreateRideUseCase createRideUseCase() {
        return new DefaultCreateRideUseCase(rideGateway, accountClient, sentEventService);
    }

    @Bean
    public UpdateRideUseCase updateRideUseCase() {
        return new DefaultUpdateRideUseCase(rideGateway, driverNotifier);
    }

    @Bean
    public AcceptRideUseCase acceptRideUseCase() {
        return new DefaultAcceptRideUseCase(rideGateway, accountClient, rideStatusCache, driverNotifier);
    }

    @Bean
    public GetRideByIdUseCase getRideByIdUseCase() {
        return new DefaultGetRideByIdUseCase(rideGateway);
    }

    @Bean
    public GetRideStatusUseCase getRideStatusUseCase() {
        return new DefaultGetRideStatusUseCase(rideStatusCache, rideGateway);
    }

    @Bean
    public ListRidesUseCase listRidesUseCase() {
        return new DefaultListRidesUseCase(rideGateway);
    }

    @Bean
    public ChangeStatusRideUseCase changeStatusRideUseCase() {
        return new DefaultChangeStatusRideUseCase(rideGateway);
    }

    @Bean
    public TimeoutRidesUseCase timeoutRidesUseCase() {
        return new DefaultTimeoutRidesUseCase(rideGateway, rideStatusCache, driverNotifier);
    }
}