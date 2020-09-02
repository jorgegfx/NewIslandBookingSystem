package com.newisland.gateway;

import com.newisland.common.dto.utils.TimeUtils;
import com.newisland.common.messages.command.ReservationCommandOuterClass;
import com.newisland.gateway.dto.CreateReservationDto;
import com.newisland.gateway.dto.UpdateReservationDto;
import com.newisland.gateway.serializer.ReservationCommandProtobufSerializer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static com.newisland.common.messages.command.ReservationCommandOuterClass.ReservationCommand.ActionType.*;

/**
 * Gateway Application
 */
@Slf4j
@SpringBootApplication
@EnableConfigurationProperties(UriConfiguration.class)
@RestController
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Autowired
    private KafkaTemplate<String, ReservationCommandOuterClass.ReservationCommand> kafkaTemplate;

    @Value("${reservation-topic}")
    private String reservationTopic;

    @DeleteMapping("/cancelReservation/{campsiteId}/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public Mono<Void> cancelReservation(@PathVariable String campsiteId,@PathVariable String id){
        try {
            ReservationCommandOuterClass.CancelReservationCommand cancel =
                    ReservationCommandOuterClass.CancelReservationCommand.newBuilder().
                            setId(id).build();
            ReservationCommandOuterClass.ReservationCommand cmd =
                    ReservationCommandOuterClass.ReservationCommand.newBuilder().
                            setActionType(CANCEL).setCancel(cancel).build();
            kafkaTemplate.send(reservationTopic, campsiteId, cmd);
        }catch (Exception ex){
            log.error("Error canceling reservation",ex);
        }
        return Mono.empty().then();
    }

    @PatchMapping("/updateReservation/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<Void> updateReservation(@PathVariable String id,@RequestBody UpdateReservationDto updateReservationDto){
        try {
            ReservationCommandOuterClass.UpdateReservationCommand update =
                    ReservationCommandOuterClass.UpdateReservationCommand.newBuilder().
                            setId(id).
                            setCampsiteId(updateReservationDto.getCampsiteId()).
                            setArrivalDate(TimeUtils.convertTimestamp(updateReservationDto.getArrivalDate())).
                            setDepartureDate(TimeUtils.convertTimestamp(updateReservationDto.getDepartureDate())).
                            build();
            ReservationCommandOuterClass.ReservationCommand cmd =
                    ReservationCommandOuterClass.ReservationCommand.newBuilder().
                            setActionType(UPDATE).setUpdate(update).build();
            kafkaTemplate.send(reservationTopic, updateReservationDto.getCampsiteId(), cmd);
        }catch (Exception ex){
            log.error("Error updating reservation",ex);
        }
        return Mono.empty().then();
    }

    @PostMapping("/createReservation")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Mono<Void> createReservation(@RequestBody CreateReservationDto createReservationDto) {
        try {
            ReservationCommandOuterClass.CreateReservationCommand create =
                    ReservationCommandOuterClass.CreateReservationCommand.newBuilder().
                            setCampsiteId(createReservationDto.getCampsiteId()).
                            setUserEmail(createReservationDto.getUserEmail()).
                            setUserFullName(createReservationDto.getUserFullName()).
                            setArrivalDate(TimeUtils.convertTimestamp(createReservationDto.getArrivalDate())).
                            setDepartureDate(TimeUtils.convertTimestamp(createReservationDto.getDepartureDate())).
                            build();
            ReservationCommandOuterClass.ReservationCommand cmd =
                    ReservationCommandOuterClass.ReservationCommand.newBuilder().
                            setActionType(CREATE).setCreate(create).build();
            kafkaTemplate.send(reservationTopic, createReservationDto.getCampsiteId(), cmd);
        }catch (Exception ex){
            log.error("Error creating reservation",ex);
        }
        return Mono.empty().then();
    }

    /**
     * Routes are build programmatically to support changes on the profile (local, kubernetes ...)
     * but is possible as well to have all routes in a application.yml
     */
    @Bean
    public RouteLocator newIslandRoutes(RouteLocatorBuilder builder, UriConfiguration uriConfiguration) {
        return builder.routes()
                .route(p -> p
                        .path("/user/**")
                        .uri(uriConfiguration.getUserServiceUrl()))
                .route(p -> p
                        .path("/campsite/**")
                        .uri(uriConfiguration.getCampsiteCatalogServiceUrl()))
                .route(p -> p
                        .path("/reservation/**")
                        .uri(uriConfiguration.getReservationServiceUrl()))
                .build();
    }

}

@Data
@ConfigurationProperties
class UriConfiguration {

    @Value("${user-service-url}")
    private String userServiceUrl;

    @Value("${reservation-service-url}")
    private String reservationServiceUrl;

    @Value("${campsite-catalog-service}")
    private String campsiteCatalogServiceUrl;

}