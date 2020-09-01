package com.newisland.reservation.controller;

import com.newisland.reservation.TestApp;
import com.newisland.reservation.dto.ReservationDto;
import com.newisland.reservation.model.entity.Reservation;
import com.newisland.reservation.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(ReservationController.class)
@ContextConfiguration(classes = TestApp.class)
class ReservationControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ReservationService reservationService;

    @Test
    public void testGetReservation() {
        UUID id = UUID.randomUUID();
        Instant createdOn = Instant.now().minus(2,ChronoUnit.DAYS);
        Instant arrivalDate = createdOn.plus(2, ChronoUnit.DAYS);
        Instant departureDate = arrivalDate.plus(2, ChronoUnit.DAYS);
        Reservation reservation = Reservation.builder().
                id(id).
                campsiteId(UUID.randomUUID()).
                userId(UUID.randomUUID()).
                arrivalDate(arrivalDate).
                departureDate(departureDate).
                createdOn(createdOn).build();
        when(reservationService.findById(id)).thenReturn(Optional.of(reservation));
        webTestClient.get()
                .uri("/reservation/"+id)
                .accept(MediaType.APPLICATION_JSON)
                .header("Accept-Language","en-CA")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ReservationDto.class)
                .value(res -> res.getId(), equalTo(id));
    }

    @Test
    public void testsGetAvailabilityByCampsite() {
        UUID campsiteID = UUID.randomUUID();
        Instant createdOn = Instant.now().minus(2,ChronoUnit.DAYS);
        Instant arrivalDate = createdOn.plus(2, ChronoUnit.DAYS);
        Instant departureDate = arrivalDate.plus(2, ChronoUnit.DAYS);
        Reservation reservation = Reservation.builder().
                id(UUID.randomUUID()).
                campsiteId(UUID.randomUUID()).
                userId(UUID.randomUUID()).
                arrivalDate(arrivalDate).
                departureDate(departureDate).
                createdOn(createdOn).build();
        List<Reservation> reservationList = Arrays.asList(reservation);
        when(reservationService.findByCampsiteAndStartDateBetween(
                any(UUID.class),
                any(Instant.class),
                any(Instant.class))).thenReturn(reservationList);
        webTestClient.get()
                .uri("/reservation/availability/"+campsiteID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ReservationDto.class)
                .value(res -> res.size(), equalTo(1));
    }


}