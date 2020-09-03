package com.newisland.reservation.command.listener;

import com.newisland.common.dto.utils.TimeUtils;
import com.newisland.common.messages.command.ReservationCommandOuterClass;
import com.newisland.common.messages.event.ReservationEventOuterClass;
import com.newisland.reservation.client.UserServiceClient;
import com.newisland.reservation.model.entity.Reservation;
import com.newisland.reservation.model.exception.ReservationException;
import com.newisland.reservation.service.ReservationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static com.newisland.common.messages.event.ReservationEventOuterClass.ReservationEvent.ActionType.CREATED;
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

    private void onCreate(ReservationCommandOuterClass.CreateReservationCommand create){
        UUID userId = userServiceClient.createUser(create.getUserEmail(), create.getUserFullName());
        Reservation reservation = Reservation.builder().
                campsiteId(UUID.fromString(create.getCampsiteId())).
                userId(userId).
                referenceId(UUID.fromString(create.getReferenceId())).
                arrivalDate(TimeUtils.convertToInstant(create.getArrivalDate())).
                departureDate(TimeUtils.convertToInstant(create.getDepartureDate())).
                build();
        ReservationEventOuterClass.ReservationCreatedEvent createdEvent =
                ReservationEventOuterClass.ReservationCreatedEvent.newBuilder().
                        setReferenceId(create.getReferenceId()).build();
        try {
            reservationService.save(reservation);
            ReservationEventOuterClass.ReservationEvent event =
                    ReservationEventOuterClass.ReservationEvent.newBuilder().
                            setActionType(CREATED).
                            setResultType(SUCCESS).
                            setCreated(createdEvent).build();
            kafkaTemplate.send(reservationEventsTopic,create.getCampsiteId(),event);
        }catch (ReservationException ex){
            ReservationEventOuterClass.ReservationEvent event =
                    ReservationEventOuterClass.ReservationEvent.newBuilder().
                            setActionType(CREATED).
                            setResultType(ERROR).
                            setErrorMessage(ex.getMessage()).
                            setCreated(createdEvent).build();
            kafkaTemplate.send(reservationEventsTopic,create.getCampsiteId(),event);
            log.error("Error",ex);
        }
    }

    private void onUpdate(ReservationCommandOuterClass.UpdateReservationCommand update){
        ReservationEventOuterClass.ReservationUpdatedEvent updatedEvent =
                ReservationEventOuterClass.ReservationUpdatedEvent.newBuilder().
                        setId(update.getId()).build();
        try {
            Reservation reservation = Reservation.builder().
                    id(UUID.fromString(update.getId())).
                    campsiteId(UUID.fromString(update.getCampsiteId())).
                    arrivalDate(TimeUtils.convertToInstant(update.getArrivalDate())).
                    departureDate(TimeUtils.convertToInstant(update.getDepartureDate())).
                    build();
            reservationService.update(reservation);
            ReservationEventOuterClass.ReservationEvent event =
                    ReservationEventOuterClass.ReservationEvent.newBuilder().
                            setActionType(CREATED).
                            setResultType(SUCCESS).
                            setUpdated(updatedEvent).build();
            kafkaTemplate.send(reservationEventsTopic, update.getCampsiteId(), event);
        }catch (ReservationException ex){
            ReservationEventOuterClass.ReservationEvent event =
                    ReservationEventOuterClass.ReservationEvent.newBuilder().
                            setActionType(CREATED).
                            setResultType(ERROR).
                            setErrorMessage(ex.getMessage()).
                            setUpdated(updatedEvent).build();
            kafkaTemplate.send(reservationEventsTopic,update.getCampsiteId(),event);
            log.error("Error",ex);
        }
    }

    private void onCancel(ReservationCommandOuterClass.CancelReservationCommand cancel){
        ReservationEventOuterClass.ReservationCancelledEvent cancelledEvent =
                ReservationEventOuterClass.ReservationCancelledEvent.newBuilder().
                        setId(cancel.getId()).build();
        Optional<Reservation> res = reservationService.cancel(UUID.fromString(cancel.getId()));
        res.ifPresent(reservation -> {
            ReservationEventOuterClass.ReservationEvent event =
                    ReservationEventOuterClass.ReservationEvent.newBuilder().
                            setActionType(CREATED).
                            setResultType(SUCCESS).
                            setCancelled(cancelledEvent).build();
            kafkaTemplate.send(reservationEventsTopic,reservation.getCampsiteId().toString(),event);
        });
    }

    @KafkaListener(topics = "${reservation-commands-topic}", groupId = "ReservationCommandsConsumerGroup")
    public void consume(ConsumerRecord<String, ReservationCommandOuterClass.ReservationCommand> message) {
        log.debug(String.format("Consumed message -> %s", message.key()));
        ReservationCommandOuterClass.ReservationCommand cmd =  message.value();
        try {
            switch (cmd.getActionType()){
                case CREATE: this.onCreate(cmd.getCreate());break;
                case UPDATE: this.onUpdate(cmd.getUpdate());break;
                case CANCEL: this.onCancel(cmd.getCancel());
            }
        }catch (Exception ex){
            log.error("Error processing message ...",ex);
            //Error handling on a error topic or other mechanism
        }
    }
}
