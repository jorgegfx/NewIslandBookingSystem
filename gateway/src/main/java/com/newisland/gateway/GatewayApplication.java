package com.newisland.gateway;

import com.newisland.common.dto.utils.TimeUtils;
import com.newisland.common.messages.command.ReservationCommandOuterClass;
import com.newisland.gateway.dto.CreateReservationDto;
import com.newisland.gateway.dto.UpdateReservationDto;
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
import org.springframework.web.bind.annotation.*;

import static com.newisland.common.messages.command.ReservationCommandOuterClass.ReservationCommand.ActionType.*;

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

    @DeleteMapping(value = "/cancelReservation/{campsiteId}/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public void cancelReservation(@PathVariable String campsiteId,@PathVariable String id){
        ReservationCommandOuterClass.CancelReservationCommand cancel =
                ReservationCommandOuterClass.CancelReservationCommand.newBuilder().
                        setId(id).build();
        ReservationCommandOuterClass.ReservationCommand cmd =
                ReservationCommandOuterClass.ReservationCommand.newBuilder().
                        setActionType(CANCEL).setCancel(cancel).build();
        byte[] messageToWrite = cmd.toByteArray();
        kafkaTemplate.send(reservationTopic,campsiteId,messageToWrite);
    }

    @PatchMapping(value = "/updateReservation/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void updateReservation(@PathVariable String id,@RequestBody UpdateReservationDto updateReservationDto){
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
        byte[] messageToWrite = cmd.toByteArray();
        kafkaTemplate.send(reservationTopic,updateReservationDto.getCampsiteId(),messageToWrite);
    }

    @PostMapping(value = "/createReservation")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void createReservation(@RequestBody CreateReservationDto createReservationDto) {
        ReservationCommandOuterClass.CreateReservationCommand create =
                ReservationCommandOuterClass.CreateReservationCommand.newBuilder().
                        setCampsiteId(createReservationDto.getCampsiteId()).
                        setUserEmail(createReservationDto.getUserEmail()).
                        setUserFullName(createReservationDto.getUserEmail()).
                        setArrivalDate(TimeUtils.convertTimestamp(createReservationDto.getArrivalDate())).
                        setDepartureDate(TimeUtils.convertTimestamp(createReservationDto.getDepartureDate())).
                        build();
        ReservationCommandOuterClass.ReservationCommand cmd =
                ReservationCommandOuterClass.ReservationCommand.newBuilder().
                        setActionType(CREATE).setCreate(create).build();
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