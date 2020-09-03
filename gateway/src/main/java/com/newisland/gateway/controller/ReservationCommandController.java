package com.newisland.gateway.controller;

import com.newisland.common.messages.command.ReservationCommandOuterClass;
import com.newisland.gateway.dto.CreateReservationDto;
import com.newisland.gateway.dto.ReservationResponse;
import com.newisland.gateway.dto.UpdateReservationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.newisland.common.messages.command.ReservationCommandOuterClass.ReservationCommand.ActionType.*;
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
            kafkaTemplate.send(reservationTopic, updateReservationDto.getCampsiteId(),
                    updateReservationDto.toProtobuf(id));
        }catch (Exception ex){
            log.error("Error updating reservation",ex);
        }
        return Mono.empty().then();
    }

    @PostMapping("/createReservation")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Mono<ReservationResponse> createReservation(@RequestBody CreateReservationDto createReservationDto) {
        try {
            UUID referenceId = UUID.randomUUID();
            kafkaTemplate.send(reservationTopic, createReservationDto.getCampsiteId(),
                    createReservationDto.toProtobuf(referenceId));
            return Mono.just(ReservationResponse.builder().referenceId(referenceId).build());
        }catch (Exception ex){
            String errorMessage = "Error creating reservation";
            log.error(errorMessage,ex);
            return Mono.error(new IllegalStateException(errorMessage));
        }
    }
}
