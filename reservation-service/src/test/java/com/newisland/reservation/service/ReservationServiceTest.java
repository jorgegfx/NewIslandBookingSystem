package com.newisland.reservation.service;

import com.newisland.reservation.model.entity.Reservation;
import com.newisland.reservation.model.entity.ReservationStatus;
import com.newisland.reservation.model.exception.AheadDaysReservationException;
import com.newisland.reservation.model.exception.AlreadyBookedReservationException;
import com.newisland.reservation.model.exception.PassMaxRangeReservationException;
import com.newisland.reservation.model.repository.ReservationRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * All Test Cases for the Reservation
 * Having
 * Max allowed booking range days = 3
 * Min allowed booking ahead days = 1
 * Max allowed booking ahead days = 30
 */
class ReservationServiceTest {

    private ReservationRepository reservationRepository = mock(ReservationRepository.class);

    private ReservationService reservationService =
            new ReservationServiceImpl(3,1,30,reservationRepository);
    /**
     * Test - The campsite can be reserved for max 3 days.
     * Failure scenario
     */
    @Test
    public void testCampsiteShouldBeBookedWithMaxRangeDaysFailed(){
        UUID campsiteId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant createdOn = now;
        Instant arrivalDate = createdOn.plus(2, ChronoUnit.DAYS);
        Instant departureDate = arrivalDate.plus(5, ChronoUnit.DAYS);
        Reservation reservation = Reservation.builder().
                userId(UUID.randomUUID()).
                arrivalDate(arrivalDate).
                departureDate(departureDate).
                createdOn(createdOn).
                campsiteId(campsiteId).build();
        assertThrows(PassMaxRangeReservationException.class,()->reservationService.save(reservation));
    }

    /**
     * Test - The campsite can be reserved for max 3 days.
     * Success scenario
     */
    @Test
    public void testCampsiteShouldBeBookedWithMaxRangeDaysSuccess(){
        UUID campsiteId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant createdOn = now;
        Instant arrivalDate = createdOn.plus(2, ChronoUnit.DAYS);
        Instant departureDate = arrivalDate.plus(3, ChronoUnit.DAYS);
        Reservation reservation = Reservation.builder().
                userId(UUID.randomUUID()).
                arrivalDate(arrivalDate).
                departureDate(departureDate).
                createdOn(createdOn).
                campsiteId(campsiteId).build();
        when(reservationRepository.
                countAvailability(
                        reservation.getCampsiteId(),
                        ReservationStatus.ACTIVE,
                        reservation.getArrivalDate(),
                        reservation.getDepartureDate())).thenReturn(0L);
        Reservation expected = Reservation.builder().
                id(UUID.randomUUID()).
                userId(UUID.randomUUID()).
                arrivalDate(arrivalDate).
                departureDate(departureDate).
                createdOn(createdOn).
                campsiteId(campsiteId).build();
        when(reservationRepository.save(reservation)).thenReturn(expected);
        Reservation res = reservationService.save(reservation);
        assertEquals(expected,res);
    }

    /**
     * The campsite can be reserved minimum 1 day(s) ahead of arrival
     * Failure scenario
     */
    @Test
    public void testCampsiteShouldBeBookedWithMinDayAheadFailed(){
        UUID campsiteId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant createdOn = now;
        Instant arrivalDate = createdOn;
        Instant departureDate = arrivalDate.plus(2, ChronoUnit.DAYS);
        Reservation reservation = Reservation.builder().
                userId(UUID.randomUUID()).
                arrivalDate(arrivalDate).
                departureDate(departureDate).
                createdOn(createdOn).
                campsiteId(campsiteId).build();
        assertThrows(AheadDaysReservationException.class,()->reservationService.save(reservation));
    }

    /**
     * The campsite can be reserved minimum 1 day(s) ahead of arrival
     * Success Scenario
     */
    @Test
    public void testCampsiteShouldBeBookedWithMinDayAheadSuccess(){
        UUID campsiteId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant createdOn = now;
        // Adding one min more since time diff
        Instant arrivalDate = createdOn.plus(1,ChronoUnit.DAYS).plus(1,ChronoUnit.MINUTES);
        Instant departureDate = arrivalDate.plus(2, ChronoUnit.DAYS);
        Reservation reservation = Reservation.builder().
                userId(UUID.randomUUID()).
                arrivalDate(arrivalDate).
                departureDate(departureDate).
                createdOn(createdOn).
                campsiteId(campsiteId).build();
        when(reservationRepository.
                countAvailability(
                        reservation.getCampsiteId(),
                        ReservationStatus.ACTIVE,
                        reservation.getArrivalDate(),
                        reservation.getDepartureDate())).thenReturn(0L);
        Reservation expected = Reservation.builder().
                id(UUID.randomUUID()).
                userId(UUID.randomUUID()).
                arrivalDate(arrivalDate).
                departureDate(departureDate).
                createdOn(createdOn).
                campsiteId(campsiteId).build();
        when(reservationRepository.save(reservation)).thenReturn(expected);
        Reservation res = reservationService.save(reservation);
        assertEquals(expected,res);
    }

    /**
     * The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance.
     * Failure scenario
     */
    @Test
    public void testCampsiteShouldBeBookedWithMaxDaysAheadFailed(){
        UUID campsiteId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant createdOn = now;
        // Adding one min more since time diff
        Instant arrivalDate = createdOn.plus(31, ChronoUnit.DAYS).plus(1,ChronoUnit.MINUTES);
        Instant departureDate = arrivalDate.plus(2, ChronoUnit.DAYS);
        Reservation reservation = Reservation.builder().
                userId(UUID.randomUUID()).
                arrivalDate(arrivalDate).
                departureDate(departureDate).
                createdOn(createdOn).
                campsiteId(campsiteId).build();
        assertThrows(AheadDaysReservationException.class,()->reservationService.save(reservation));
    }

    /**
     * Already Booked Scenario
     * Failure Scenario
     */
    @Test
    public void testCampsiteShouldNotBeDoubleBooked(){
        UUID campsiteId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant createdOn = now;
        // Adding one min more since time diff
        Instant arrivalDate = createdOn.plus(1,ChronoUnit.DAYS).plus(1,ChronoUnit.MINUTES);
        Instant departureDate = arrivalDate.plus(2, ChronoUnit.DAYS);
        Reservation reservation = Reservation.builder().
                userId(UUID.randomUUID()).
                arrivalDate(arrivalDate).
                departureDate(departureDate).
                createdOn(createdOn).
                campsiteId(campsiteId).build();
        when(reservationRepository.
                countAvailability(
                        any(UUID.class),
                        any(ReservationStatus.class),
                        any(Instant.class),
                        any(Instant.class))).thenReturn(1L);
        when(reservationRepository.
                countAvailability(
                        reservation.getCampsiteId(),
                        ReservationStatus.ACTIVE,
                        reservation.getArrivalDate(),
                        reservation.getDepartureDate())).thenReturn(1L);
        assertThrows(AlreadyBookedReservationException.class,()->reservationService.save(reservation));
    }
}