package com.newisland.reservation.dto;

import com.newisland.common.dto.utils.TimeUtils;
import com.newisland.reservation.model.entity.Reservation;
import com.newisland.reservation.model.entity.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationDto {
    private UUID id;
    private UUID campsiteId;
    private ZonedDateTime arrivalDate;
    private ZonedDateTime departureDate;
    private ReservationStatus status;
    private ZonedDateTime createdOn;
    private Optional<ZonedDateTime> updatedOn;


    public static ReservationDto fromDomain(Reservation reservation, TimeZone timeZone){
        Optional<Instant> updatedOn =
                (reservation.getUpdatedOn()!=null)?Optional.of(reservation.getUpdatedOn()):Optional.empty();
        return ReservationDto.builder().
                id(reservation.getId()).
                campsiteId(reservation.getCampsiteId()).
                arrivalDate(TimeUtils.convertToZonedDateTime(reservation.getArrivalDate(),timeZone)).
                departureDate(TimeUtils.convertToZonedDateTime(reservation.getDepartureDate(),timeZone)).
                status(reservation.getStatus()).
                createdOn(TimeUtils.convertToZonedDateTime(reservation.getCreatedOn(),timeZone)).
                updatedOn(updatedOn.map(time->TimeUtils.convertToZonedDateTime(time,timeZone))).
                build();
    }
}
