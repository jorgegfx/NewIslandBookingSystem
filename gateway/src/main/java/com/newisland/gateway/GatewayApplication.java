package com.newisland.gateway;

import com.google.protobuf.Timestamp;
import com.newisland.gateway.dto.CreateReservationDto;
import lombok.Data;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.newisland.common.messages.command.*;

import java.time.Instant;
import java.time.ZonedDateTime;
/**
 * Gateway Application
 */
@SpringBootApplication
@EnableConfigurationProperties(UriConfiguration.class)
@RestController
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Autowired
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    @Value("${reservation-topic}")
    private String reservationTopic;

    private Timestamp convertTimestamp(ZonedDateTime zonedDateTime){
        Instant time = zonedDateTime.toInstant();
        return Timestamp.newBuilder().setSeconds(time.getEpochSecond())
                .setNanos(time.getNano()).build();
    }

    @PostMapping(value = "/createReservation")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void createReservation(@RequestBody CreateReservationDto createReservationDto) {
        CreateReservationCommandOuterClass.CreateReservationCommand cmd =
                CreateReservationCommandOuterClass.CreateReservationCommand.newBuilder().
                        setCampsiteId(createReservationDto.getCampsiteId()).
                        setUserEmail(createReservationDto.getUserEmail()).
                        setUserFullName(createReservationDto.getUserEmail()).
                        setArrivalDate(convertTimestamp(createReservationDto.getArrivalDate())).
                        setDepartureDate(convertTimestamp(createReservationDto.getDepartureDate())).
                        build();
        byte[] messageToWrite = cmd.toByteArray();
        kafkaTemplate.send(reservationTopic,createReservationDto.getCampsiteId(),messageToWrite);
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