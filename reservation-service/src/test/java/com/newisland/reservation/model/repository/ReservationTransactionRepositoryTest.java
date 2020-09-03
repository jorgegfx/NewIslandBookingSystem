package com.newisland.reservation.model.repository;

import com.newisland.reservation.TestApp;
import com.newisland.reservation.model.entity.ReservationTransaction;
import com.newisland.reservation.model.entity.ReservationTransactionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.transaction.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(SpringExtension.class)
@Transactional
@ActiveProfiles(profiles = "test")
@SpringBootTest(classes = TestApp.class)
class ReservationTransactionRepositoryTest {
    @Autowired
    private ReservationTransactionRepository reservationTransactionRepository;

    @Test
    public void testFindByCorrelationId(){
        UUID correlationId = UUID.randomUUID();
        ReservationTransaction reservationTransaction  = ReservationTransaction.builder().
                createdOn(Instant.now()).
                correlationId(correlationId).
                status(ReservationTransactionStatus.ERROR).
                errorMessage("Error!").
                build();
        ReservationTransaction created = reservationTransactionRepository.save(reservationTransaction);
        Optional<ReservationTransaction> actual = reservationTransactionRepository.findByCorrelationId(correlationId);
        assertEquals(created,actual.get());
    }
}