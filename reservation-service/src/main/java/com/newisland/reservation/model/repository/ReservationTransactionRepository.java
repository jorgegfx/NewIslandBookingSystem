package com.newisland.reservation.model.repository;

import com.newisland.reservation.model.entity.ReservationTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReservationTransactionRepository extends JpaRepository<ReservationTransaction,Integer> {
    Optional<ReservationTransaction> findByCorrelationId(UUID id);
}
