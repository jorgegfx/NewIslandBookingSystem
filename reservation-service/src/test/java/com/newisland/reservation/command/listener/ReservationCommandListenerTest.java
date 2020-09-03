package com.newisland.reservation.command.listener;

import com.newisland.common.dto.utils.TimeUtils;
import com.newisland.common.messages.command.ReservationCommandOuterClass;
import com.newisland.reservation.client.UserServiceClient;
import com.newisland.reservation.model.entity.Reservation;
import com.newisland.reservation.service.ReservationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.record.TimestampType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static com.newisland.common.messages.command.ReservationCommandOuterClass.ReservationCommand.ActionType.CREATE;
import static com.newisland.common.messages.command.ReservationCommandOuterClass.ReservationCommand.ActionType.UPDATE;
import static org.mockito.Mockito.*;

class ReservationCommandListenerTest {
    private ReservationService reservationService = mock(ReservationService.class);
    private UserServiceClient userServiceClient = mock(UserServiceClient.class);
    private ReservationCommandListener reservationCommandListener =
            new ReservationCommandListener(reservationService, userServiceClient);

/*
    @Test
    public void testOnConsumeCreate() {
        String campsiteId = UUID.randomUUID().toString();
        String email = "test@test.com";
        String userFullName = "Some User";
        UUID userId = UUID.randomUUID();
        ZonedDateTime arrivalDate = ZonedDateTime.now().plus(5, ChronoUnit.DAYS);
        ZonedDateTime departureDate = arrivalDate.plus(1, ChronoUnit.DAYS);
        ReservationCommandOuterClass.CreateReservationCommand create =
                ReservationCommandOuterClass.CreateReservationCommand.newBuilder().
                        setCampsiteId(campsiteId).
                        setUserEmail(email).
                        setUserFullName(userFullName).
                        setArrivalDate(TimeUtils.convertTimestamp(arrivalDate)).
                        setDepartureDate(TimeUtils.convertTimestamp(departureDate)).
                        build();
        ReservationCommandOuterClass.ReservationCommand cmd =
                ReservationCommandOuterClass.ReservationCommand.newBuilder().
                        setActionType(CREATE).setCreate(create).build();
        byte[] input = cmd.toByteArray();
        ConsumerRecord<String, byte[]> consumerRecord = new ConsumerRecord<>(
                "topic", 1, 0, 0L,
                TimestampType.CREATE_TIME, 0L, 0, 0, "1",
                input);
        when(userServiceClient.createUser(email,userFullName)).thenReturn(userId);
        Reservation reservation = Reservation.builder().
                campsiteId(UUID.fromString(create.getCampsiteId())).
                userId(userId).
                arrivalDate(Instant.ofEpochMilli(create.getArrivalDate().getSeconds())).
                arrivalDate(Instant.ofEpochMilli(create.getDepartureDate().getSeconds())).
                build();
        when(reservationService.save(reservation)).thenReturn(reservation);
        reservationCommandListener.consume(consumerRecord);

        verify(userServiceClient).createUser(email,userFullName);
        verify(reservationService).save(reservation);
    }

    @Test
    public void testOnConsumeUpdate() {
        String id = UUID.randomUUID().toString();
        String campsiteId = UUID.randomUUID().toString();
        String email = "test@test.com";
        String userFullName = "Some User";
        UUID userId = UUID.randomUUID();
        ZonedDateTime arrivalDate = ZonedDateTime.now().plus(5, ChronoUnit.DAYS);
        ZonedDateTime departureDate = arrivalDate.plus(1, ChronoUnit.DAYS);
        ReservationCommandOuterClass.UpdateReservationCommand update =
                ReservationCommandOuterClass.UpdateReservationCommand.newBuilder().
                        setId(id).
                        setCampsiteId(campsiteId).
                        setArrivalDate(TimeUtils.convertTimestamp(arrivalDate)).
                        setDepartureDate(TimeUtils.convertTimestamp(departureDate)).
                        build();
        ReservationCommandOuterClass.ReservationCommand cmd =
                ReservationCommandOuterClass.ReservationCommand.newBuilder().
                        setActionType(UPDATE).setUpdate(update).build();
        byte[] input = cmd.toByteArray();
        ConsumerRecord<String, byte[]> consumerRecord = new ConsumerRecord<>(
                "topic", 1, 0, 0L,
                TimestampType.CREATE_TIME, 0L, 0, 0, "1",
                input);
        Reservation reservation = Reservation.builder().
                id(UUID.fromString(id)).
                campsiteId(UUID.fromString(campsiteId)).
                userId(userId).
                arrivalDate(arrivalDate.toInstant()).
                arrivalDate(departureDate.toInstant()).
                build();
        when(reservationService.update(reservation)).thenReturn(Optional.of(reservation));
        reservationCommandListener.consume(consumerRecord);
        verify(reservationService).update(reservation);
    }*/
}