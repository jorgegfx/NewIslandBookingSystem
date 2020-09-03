package com.newisland.reservation.command.listener;

import com.newisland.common.dto.utils.TimeUtils;
import com.newisland.common.messages.command.ReservationCommandOuterClass;
import com.newisland.common.messages.event.ReservationEventOuterClass;
import com.newisland.reservation.client.UserServiceClient;
import com.newisland.reservation.model.entity.Reservation;
import com.newisland.reservation.model.entity.ReservationTransaction;
import com.newisland.reservation.model.entity.ReservationTransactionStatus;
import com.newisland.reservation.model.exception.ReservationException;
import com.newisland.reservation.service.ReservationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static com.newisland.common.messages.event.ReservationEventOuterClass.ReservationEvent.ActionType.*;
import static com.newisland.common.messages.event.ReservationEventOuterClass.ReservationEvent.ResultType.ERROR;
import static com.newisland.common.messages.event.ReservationEventOuterClass.ReservationEvent.ResultType.SUCCESS;

@Slf4j
@Service
public class ReservationCommandListener {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private KafkaTemplate<String, ReservationEventOuterClass.ReservationEvent> kafkaTemplate;

    @Value("${reservation-events-topic}")
    private String reservationEventsTopic;

    public ReservationCommandListener() {
    }

    public ReservationCommandListener(ReservationService reservationService, UserServiceClient userServiceClient) {
        this.reservationService = reservationService;
        this.userServiceClient = userServiceClient;
    }

    private void publishError(UUID correlationId,
                              String campsiteId,
                              ReservationEventOuterClass.ReservationEvent event,
                              Throwable ex){
        kafkaTemplate.send(reservationEventsTopic,campsiteId,event);
        reservationService.save(ReservationTransaction.builder().
                correlationId(correlationId).
                status(ReservationTransactionStatus.ERROR).
                errorMessage(ex.getMessage().substring(0,255)).
                build());
    }

    @CacheEvict(cacheNames="campsiteAvailability", key = "#create.campsiteId")
    private void onCreate(ReservationCommandOuterClass.ReservationCommand cmd){
        ReservationCommandOuterClass.CreateReservationCommand create = cmd.getCreate();
        UUID userId = userServiceClient.createUser(create.getUserEmail(), create.getUserFullName());
        Reservation reservation = Reservation.builder().
                campsiteId(UUID.fromString(create.getCampsiteId())).
                userId(userId).
                arrivalDate(TimeUtils.convertToInstant(create.getArrivalDate())).
                departureDate(TimeUtils.convertToInstant(create.getDepartureDate())).
                build();
        ReservationEventOuterClass.ReservationCreatedEvent createdEvent =
                ReservationEventOuterClass.ReservationCreatedEvent.newBuilder().
                        build();
        UUID correlationId = UUID.fromString(cmd.getCorrelationId());
        try {
            reservationService.save(reservation,correlationId);
            ReservationEventOuterClass.ReservationEvent event =
                    ReservationEventOuterClass.ReservationEvent.newBuilder().
                            setCorrelationId(cmd.getCorrelationId()).
                            setActionType(CREATED).
                            setResultType(SUCCESS).
                            setCreated(createdEvent).build();
            kafkaTemplate.send(reservationEventsTopic,create.getCampsiteId(),event);
        }catch (ReservationException ex){
            log.error("Error",ex);
            ReservationEventOuterClass.ReservationEvent event =
                    ReservationEventOuterClass.ReservationEvent.newBuilder().
                            setCorrelationId(cmd.getCorrelationId()).
                            setActionType(CREATED).
                            setResultType(ERROR).
                            setErrorMessage(ex.getMessage()).
                            setCreated(createdEvent).build();
            publishError(correlationId,create.getCampsiteId(),event,ex);
        }
    }

    @CacheEvict(cacheNames="campsiteAvailability", key = "#create.campsiteId")
    private void onUpdate(ReservationCommandOuterClass.ReservationCommand cmd){
        ReservationCommandOuterClass.UpdateReservationCommand update = cmd.getUpdate();
        ReservationEventOuterClass.ReservationUpdatedEvent updatedEvent =
                ReservationEventOuterClass.ReservationUpdatedEvent.newBuilder().
                        setId(update.getId()).
                        build();
        UUID correlationId = UUID.fromString(cmd.getCorrelationId());
        try {
            Reservation reservation = Reservation.builder().
                    id(UUID.fromString(update.getId())).
                    campsiteId(UUID.fromString(update.getCampsiteId())).
                    arrivalDate(TimeUtils.convertToInstant(update.getArrivalDate())).
                    departureDate(TimeUtils.convertToInstant(update.getDepartureDate())).
                    build();
            reservationService.update(reservation,correlationId);
            ReservationEventOuterClass.ReservationEvent event =
                    ReservationEventOuterClass.ReservationEvent.newBuilder().
                            setCorrelationId(cmd.getCorrelationId()).
                            setActionType(UPDATED).
                            setResultType(SUCCESS).
                            setUpdated(updatedEvent).build();
            kafkaTemplate.send(reservationEventsTopic, update.getCampsiteId(), event);
        }catch (ReservationException ex){
            log.error("Error",ex);
            ReservationEventOuterClass.ReservationEvent event =
                    ReservationEventOuterClass.ReservationEvent.newBuilder().
                            setCorrelationId(cmd.getCorrelationId()).
                            setActionType(UPDATED).
                            setResultType(ERROR).
                            setErrorMessage(ex.getMessage()).
                            setUpdated(updatedEvent).build();
            publishError(correlationId,update.getCampsiteId(),event,ex);
        }
    }

    @CacheEvict(cacheNames="campsiteAvailability", key = "#create.campsiteId")
    private void onCancel(ReservationCommandOuterClass.ReservationCommand cmd){
        ReservationCommandOuterClass.CancelReservationCommand cancel = cmd.getCancel();
        ReservationEventOuterClass.ReservationCancelledEvent cancelledEvent =
                ReservationEventOuterClass.ReservationCancelledEvent.newBuilder().
                        setId(cancel.getId()).
                        build();
        Optional<Reservation> res = reservationService.cancel(UUID.fromString(cancel.getId()),
                UUID.fromString(cmd.getCorrelationId()));
        res.ifPresent(reservation -> {
            ReservationEventOuterClass.ReservationEvent event =
                    ReservationEventOuterClass.ReservationEvent.newBuilder().
                            setCorrelationId(cmd.getCorrelationId()).
                            setActionType(CANCELLED).
                            setResultType(SUCCESS).
                            setCancelled(cancelledEvent).build();
            kafkaTemplate.send(reservationEventsTopic,reservation.getCampsiteId().toString(),event);
        });
    }

    @KafkaListener(topics = "${reservation-commands-topic}", groupId = "ReservationCommandsConsumerGroup")
    public void consume(ConsumerRecord<String, ReservationCommandOuterClass.ReservationCommand> message) {
        ReservationCommandOuterClass.ReservationCommand cmd =  message.value();
        log.info(String.format("Consumed message -> %s", cmd));
        try {
            switch (cmd.getActionType()){
                case CREATE: this.onCreate(cmd);break;
                case UPDATE: this.onUpdate(cmd);break;
                case CANCEL: this.onCancel(cmd);
            }
        }catch (Exception ex){
            log.error("Error processing message ...",ex);
            //Error handling on a error topic or other mechanism that will allow reprocessing
        }
    }
}
