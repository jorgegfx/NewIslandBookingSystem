package com.newisland.reservation.model.repository;

import com.newisland.reservation.TestApp;
import com.newisland.reservation.model.entity.Reservation;
import com.newisland.reservation.model.entity.ReservationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(SpringExtension.class)
@Transactional
@ActiveProfiles(profiles = "test")
@SpringBootTest(classes = TestApp.class)
class ReservationRepositoryTest {
    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    public void testFindSavedUserById() {
        Instant createdOn = Instant.now().minus(2, ChronoUnit.DAYS);
        Instant arrivalDate = createdOn.plus(2, ChronoUnit.DAYS);
        Instant departureDate = arrivalDate.plus(2, ChronoUnit.DAYS);
        Reservation reservation = Reservation.builder().
                userId(UUID.randomUUID()).
                referenceId(UUID.randomUUID()).
                arrivalDate(arrivalDate).
                departureDate(departureDate).
                createdOn(createdOn).
                campsiteId(UUID.randomUUID()).build();
        Reservation expectedReservation = reservationRepository.save(reservation);
        assertThat(reservationRepository.findById(reservation.getId())).hasValue(expectedReservation);
    }

    @Test
    public void testFindByCampsiteIdAndStatusAndArrivalDateBetween() {
        Instant createdOn = Instant.now().minus(2, ChronoUnit.DAYS);
        Instant arrivalDate = createdOn.plus(2, ChronoUnit.DAYS);
        Instant departureDate = arrivalDate.plus(2, ChronoUnit.DAYS);
        Reservation reservation = Reservation.builder().
                userId(UUID.randomUUID()).
                referenceId(UUID.randomUUID()).
                arrivalDate(arrivalDate).
                departureDate(departureDate).
                createdOn(createdOn).
                status(ReservationStatus.ACTIVE).
                campsiteId(UUID.randomUUID()).build();
        Reservation expectedReservation = reservationRepository.save(reservation);
        List<Reservation> reservations = reservationRepository.findByCampsiteIdAndStatusAndArrivalDateBetween(
                reservation.getCampsiteId(), ReservationStatus.ACTIVE,
                arrivalDate.minus(10, ChronoUnit.DAYS),
                arrivalDate.plus(10, ChronoUnit.DAYS));
        assertFalse(reservations.isEmpty());
        assertEquals(expectedReservation,reservations.get(0));
    }

    @Test
    public void testCountAvailabilityIntersectingDepartureDate(){
        UUID campsiteId = UUID.randomUUID();
        Instant createdOn = Instant.now().minus(2, ChronoUnit.DAYS);
        Instant arrivalDate = createdOn.plus(2, ChronoUnit.DAYS);
        Instant departureDate = arrivalDate.plus(3, ChronoUnit.DAYS);
        Reservation reservation = Reservation.builder().
                userId(UUID.randomUUID()).
                referenceId(UUID.randomUUID()).
                arrivalDate(arrivalDate).
                departureDate(departureDate).
                createdOn(createdOn).
                status(ReservationStatus.ACTIVE).
                campsiteId(campsiteId).build();
        reservationRepository.save(reservation);
        Instant bookingArrivalDate = arrivalDate.minus(1,ChronoUnit.DAYS);
        Instant bookingDepartureDate = bookingArrivalDate.plus(2,ChronoUnit.DAYS);
        Long count = reservationRepository.countAvailability(
                campsiteId,ReservationStatus.ACTIVE,bookingArrivalDate,bookingDepartureDate);
        assertEquals(1,count);
    }

    @Test
    public void testCountAvailabilityIntersectingArrivalDate(){
        UUID campsiteId = UUID.randomUUID();
        Instant createdOn = Instant.now().minus(2, ChronoUnit.DAYS);
        Instant arrivalDate = createdOn.plus(2, ChronoUnit.DAYS);
        Instant departureDate = arrivalDate.plus(3, ChronoUnit.DAYS);
        Reservation reservation = Reservation.builder().
                userId(UUID.randomUUID()).
                referenceId(UUID.randomUUID()).
                arrivalDate(arrivalDate).
                departureDate(departureDate).
                createdOn(createdOn).
                status(ReservationStatus.ACTIVE).
                campsiteId(campsiteId).build();
        reservationRepository.save(reservation);
        Instant bookingArrivalDate = arrivalDate.plus(1,ChronoUnit.DAYS);
        Instant bookingDepartureDate = departureDate.minus(1,ChronoUnit.DAYS);
        Long count = reservationRepository.countAvailability(
                campsiteId,ReservationStatus.ACTIVE,bookingArrivalDate,bookingDepartureDate);
        assertEquals(1,count);
    }
}