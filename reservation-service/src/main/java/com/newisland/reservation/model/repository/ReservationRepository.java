package com.newisland.reservation.model.repository;

import com.newisland.reservation.model.entity.Reservation;
import com.newisland.reservation.model.entity.ReservationStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends PagingAndSortingRepository<Reservation, UUID> {

    List<Reservation> findByCampsiteIdAndStatusAndArrivalDateBetween(UUID campsiteId,
                                                                   ReservationStatus status,
                                                                   Instant start,
                                                                   Instant end);

    @Query(value = "SELECT count(r) FROM Reservation r WHERE r.campsiteId = :campsiteId AND r.status = :status AND" +
            " ((:start BETWEEN r.arrivalDate AND r.departureDate) OR (:end BETWEEN r.arrivalDate AND r.departureDate))")
    Long countAvailability(@Param("campsiteId") UUID campsiteId,
                           @Param("status") ReservationStatus status,
                           @Param("start") Instant start,
                           @Param("end") Instant end);
}
