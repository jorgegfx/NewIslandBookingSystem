package com.newisland.reservation.service;

import com.newisland.reservation.model.entity.Reservation;
import com.newisland.reservation.model.entity.ReservationTransaction;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationService {
    Optional<Reservation> findById(UUID id);

    Optional<ReservationTransaction> findByReservationTransactionByCorrelationId(UUID correlationId);

    List<Reservation> findByCampsiteAndStartDateBetween(UUID campsiteId, Instant start, Instant end);

    boolean isAvailable(UUID campsiteId, Instant start, Instant end);

    boolean isAvailable(UUID id, UUID campsiteId, Instant start, Instant end);

    Reservation save(Reservation reservation, UUID correlationId);

    Optional<Reservation> update(Reservation reservation, UUID correlationId);

    Optional<Reservation> cancel(UUID id, UUID correlationId);

    void save(ReservationTransaction reservationTransaction);
}
