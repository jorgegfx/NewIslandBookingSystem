package com.newisland.reservation.service;

import com.google.protobuf.Timestamp;
import com.newisland.common.messages.command.CreateReservationCommandOuterClass;
import com.newisland.reservation.client.UserServiceClient;
import com.newisland.reservation.model.entity.Reservation;
import com.newisland.reservation.model.entity.ReservationStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.kafka.annotation.KafkaListener;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
public class ReservationCommandListener {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private UserServiceClient userServiceClient;

    private Instant convertToInstant(Timestamp timestamp){
        return Instant.ofEpochMilli(timestamp.getSeconds());
    }

    @KafkaListener(topics = "reservation")
    public void consume(ConsumerRecord<String, byte[]> message) {
        log.info(String.format("#### -> Consumed message -> %s", message));
        try {
            CreateReservationCommandOuterClass.CreateReservationCommand cmd =
                    CreateReservationCommandOuterClass.CreateReservationCommand.parseFrom(message.value());
            UUID userId = userServiceClient.createUser(cmd.getUserEmail(), cmd.getUserFullName());
            Reservation reservation = Reservation.builder().
                    campsiteId(UUID.fromString(cmd.getCampsiteId())).
                    userId(userId).
                    arrivalDate(convertToInstant(cmd.getArrivalDate())).
                    departureDate(convertToInstant(cmd.getDepartureDate())).
                    build();
            reservationService.save(reservation);
        }catch (Exception ex){
            log.error("Error reading message ...",ex);
        }
    }
}
