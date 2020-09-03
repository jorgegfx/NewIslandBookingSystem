package com.newisland.reservation.command.listener;

import com.newisland.common.dto.utils.TimeUtils;
import com.newisland.common.messages.command.ReservationCommandOuterClass;
import com.newisland.reservation.client.UserServiceClient;
import com.newisland.reservation.model.entity.Reservation;
import com.newisland.reservation.model.exception.ReservationException;
import com.newisland.reservation.service.ReservationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class ReservationCommandListener {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private UserServiceClient userServiceClient;

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
                arrivalDate(TimeUtils.convertToInstant(create.getArrivalDate())).
                departureDate(TimeUtils.convertToInstant(create.getDepartureDate())).
                build();
        reservationService.save(reservation);
    }

    private void onUpdate(ReservationCommandOuterClass.UpdateReservationCommand update){
        Reservation reservation = Reservation.builder().
                id(UUID.fromString(update.getId())).
                campsiteId(UUID.fromString(update.getCampsiteId())).
                arrivalDate(TimeUtils.convertToInstant(update.getArrivalDate())).
                departureDate(TimeUtils.convertToInstant(update.getDepartureDate())).
                build();
        reservationService.update(reservation);
    }

    private void onCancel(ReservationCommandOuterClass.CancelReservationCommand cancel){
        reservationService.cancel(UUID.fromString(cancel.getId()));
    }

    @KafkaListener(topics = "${reservation-topic}", groupId = "ReservationConsumerGroup")
    public void consume(ConsumerRecord<String, ReservationCommandOuterClass.ReservationCommand> message) {
        log.info(String.format("#### -> Consumed message -> %s", message.key()));
        ReservationCommandOuterClass.ReservationCommand cmd = null;
        try {
            cmd = message.value();
            switch (cmd.getActionType()){
                case CREATE: this.onCreate(cmd.getCreate());break;
                case UPDATE: this.onUpdate(cmd.getUpdate());break;
                case CANCEL: this.onCancel(cmd.getCancel());
            }

        }catch (ReservationException ex){
            String errorMessage = (cmd==null)?"Error processing message ...":
                    String.format("Error processing %s ...",cmd);
            log.error(errorMessage,ex);
        }catch (Exception ex){
            log.error("Error processing message ...",ex);
        }
    }
}
