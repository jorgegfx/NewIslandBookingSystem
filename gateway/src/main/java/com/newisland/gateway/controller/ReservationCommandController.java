package com.newisland.gateway.controller;

import com.newisland.common.messages.command.ReservationCommandOuterClass;
import com.newisland.gateway.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Fire and forget controller in the case that websockets are not enabled
 */
@Slf4j
@RestController
public class ReservationCommandController {
    @Autowired
    private KafkaTemplate<String, ReservationCommandOuterClass.ReservationCommand> kafkaTemplate;

    @Value("${reservation-commands-topic}")
    private String reservationTopic;

    @DeleteMapping("/cancelReservation/{campsiteId}/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public Mono<ReservationResponse> cancelReservation(@PathVariable String campsiteId, @PathVariable String id){
        try {
            UUID correlationId = UUID.randomUUID();
            CancelReservationRequest cancelReservationRequest = new CancelReservationRequest();
            cancelReservationRequest.setId(id);
            cancelReservationRequest.setCampsiteId(campsiteId);
            kafkaTemplate.send(reservationTopic, campsiteId,
                    cancelReservationRequest.toProtobuf(correlationId.toString()));
            return Mono.just(ReservationResponse.builder().
                    correlationId(correlationId).
                    status(ReservationStatus.PENDING).build());
        }catch (Exception ex){
            String errorMessage = "Error cancelling reservation";
            log.error(errorMessage,ex);
            return Mono.error(new IllegalStateException(errorMessage));
        }
    }

    @PatchMapping("/updateReservation/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<ReservationResponse> updateReservation(@PathVariable String id,
                                                       @RequestBody UpdateReservationRequest updateReservationRequest){
        try {
            UUID correlationId = UUID.randomUUID();
            kafkaTemplate.send(reservationTopic, updateReservationRequest.getCampsiteId(),
                    updateReservationRequest.toProtobuf(id,correlationId.toString()));
            return Mono.just(ReservationResponse.builder().
                    correlationId(correlationId).
                    status(ReservationStatus.PENDING).build());
        }catch (Exception ex){
            String errorMessage = "Error updating reservation";
            log.error(errorMessage,ex);
            return Mono.error(new IllegalStateException(errorMessage));
        }
    }

    @PostMapping("/createReservation")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Mono<ReservationResponse> createReservation(@RequestBody CreateReservationRequest createReservationRequest) {
        try {
            UUID correlationId = UUID.randomUUID();
            kafkaTemplate.send(reservationTopic, createReservationRequest.getCampsiteId(),
                    createReservationRequest.toProtobuf(correlationId));
            return Mono.just(ReservationResponse.builder().
                    correlationId(correlationId).
                    status(ReservationStatus.PENDING).build());
        }catch (Exception ex){
            String errorMessage = "Error creating reservation";
            log.error(errorMessage,ex);
            return Mono.error(new IllegalStateException(errorMessage));
        }
    }
}
