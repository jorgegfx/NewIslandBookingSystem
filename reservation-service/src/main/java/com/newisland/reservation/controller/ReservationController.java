package com.newisland.reservation.controller;

import com.newisland.reservation.dto.ReservationDto;
import com.newisland.reservation.model.entity.Reservation;
import com.newisland.reservation.service.ReservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/reservation")
public class ReservationController {
    @Autowired
    private ReservationService reservationService;

    public Flux<ReservationDto> convertToDto(List<Reservation> reservations,TimeZone timeZone){
        return Flux.fromStream(reservations.stream().
                map(reservation -> ReservationDto.fromDomain(reservation,timeZone)));
    }

    @GetMapping("/{id}")
    public Mono<ReservationDto> getReservation(@PathVariable String id){
        TimeZone userTimeZone = LocaleContextHolder.getTimeZone();
        return Mono.justOrEmpty(reservationService.
                findById(UUID.fromString(id)).map(res->ReservationDto.fromDomain(res,userTimeZone)));
    }


    @GetMapping("/availability/{campsiteId}")
    public Flux<ReservationDto> getAvailabilityByCampsite(@PathVariable String campsiteId) {
        try {
            TimeZone userTimeZone = LocaleContextHolder.getTimeZone();
            Instant now = Instant.now();
            Instant aMonthLater = now.plus(30, ChronoUnit.DAYS);
            List<Reservation> reservations =
                    reservationService.
                            findByCampsiteAndStartDateBetween(UUID.fromString(campsiteId), now, aMonthLater);
            return convertToDto(reservations,userTimeZone);
        }catch (Exception ex){
            String errorMessage = String.format("Error Listing reservations: %s ...",campsiteId);
            log.error(errorMessage,ex);
            return Flux.error(new IllegalStateException(errorMessage));
        }
    }

    @GetMapping("/availability/{campsiteId}/{timeZone}/{startDate}/{endDate}")
    public Flux<ReservationDto> getAvailabilityByCampsiteBetween(@PathVariable String campsiteId,
           @PathVariable("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
           @PathVariable("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate) {
        try {
            TimeZone userTimeZone = LocaleContextHolder.getTimeZone();
            Instant start = Instant.from(startDate);
            Instant end = Instant.from(endDate);
            if(start.isAfter(end)){
                throw new IllegalArgumentException("EndDate Should be after StartDate");
            }

            List<Reservation> reservations =
                    reservationService.
                            findByCampsiteAndStartDateBetween(UUID.fromString(campsiteId), start, end);
            return convertToDto(reservations,userTimeZone);
        }catch (Exception ex){
            String errorMessage = String.format("Error Listing reservations for: %s between %s - %s...",
                    campsiteId,startDate,endDate);
            log.error(errorMessage,ex);
            return Flux.error(new IllegalStateException(errorMessage));
        }
    }
}
