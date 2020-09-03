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
    public Mono<Void> cancelReservation(@PathVariable String campsiteId, @PathVariable String id){
        try {
            CancelReservationRequest cancelReservationRequest = new CancelReservationRequest();
            cancelReservationRequest.setId(id);
            cancelReservationRequest.setCampsiteId(campsiteId);
            kafkaTemplate.send(reservationTopic, campsiteId, cancelReservationRequest.toProtobuf());
        }catch (Exception ex){
            log.error("Error canceling reservation",ex);
        }
        return Mono.empty().then();
    }

    @PatchMapping("/updateReservation/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<Void> updateReservation(@PathVariable String id,@RequestBody UpdateReservationRequest updateReservationRequest){
        try {
            kafkaTemplate.send(reservationTopic, updateReservationRequest.getCampsiteId(),
                    updateReservationRequest.toProtobuf(id));
        }catch (Exception ex){
            log.error("Error updating reservation",ex);
        }
        return Mono.empty().then();
    }

    @PostMapping("/createReservation")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Mono<ReservationResponse> createReservation(@RequestBody CreateReservationRequest createReservationRequest) {
        try {
            UUID referenceId = UUID.randomUUID();
            kafkaTemplate.send(reservationTopic, createReservationRequest.getCampsiteId(),
                    createReservationRequest.toProtobuf(referenceId));
            return Mono.just(ReservationResponse.builder().
                    referenceId(referenceId).
                    status(ReservationStatus.PENDING).build());
        }catch (Exception ex){
            String errorMessage = "Error creating reservation";
            log.error(errorMessage,ex);
            return Mono.error(new IllegalStateException(errorMessage));
        }
    }
}
