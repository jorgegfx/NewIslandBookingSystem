package com.newisland.reservation.service;

import com.newisland.reservation.model.entity.Reservation;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationService {
    Optional<Reservation> findById(UUID id);

    List<Reservation> findByCampsiteAndStartDateBetween(UUID campsiteId, Instant start, Instant end);

    boolean isAvailable(UUID campsiteId, Instant start, Instant end);

    Reservation save(Reservation reservation);

    Optional<Reservation> update(Reservation reservation);

    Optional<Reservation> cancel(UUID id);
}
