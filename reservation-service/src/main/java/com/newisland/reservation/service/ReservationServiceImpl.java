package com.newisland.reservation.service;

import com.newisland.reservation.model.entity.Reservation;
import com.newisland.reservation.model.entity.ReservationStatus;
import com.newisland.reservation.model.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {
    @Autowired
    private ReservationRepository reservationRepository;

    @Override
    public Optional<Reservation> findById(UUID id) {
        return reservationRepository.findById(id);
    }

    @Override
    public List<Reservation> findByCampsiteAndStartDateBetween(UUID campsiteId, Instant start, Instant end) {
        return reservationRepository.
                findByCampsiteIdAndStatusAndArrivalDateBetween(campsiteId,
                        ReservationStatus.ACTIVE,start,end);
    }

    @Override
    public boolean isAvailable(UUID campsiteId, Instant start, Instant end) {
        return reservationRepository.countAvailability(campsiteId, ReservationStatus.ACTIVE, start, end) == 0;
    }

    private boolean isValid(Instant now, Reservation reservation) {
        //Reservation is not Double Booked
        if (!isAvailable(reservation.getCampsiteId(),
                reservation.getArrivalDate(),
                reservation.getDepartureDate()))
            return false;
        long numberOfDaysToReserve = Duration.between(
                reservation.getDepartureDate(),
                reservation.getArrivalDate()).toDays();
        //The campsite can be reserved for max 3 days.
        if (numberOfDaysToReserve > 3)
            return false;
        //The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance.
        long numberOfDaysAhead = Duration.between(
                now, reservation.getArrivalDate()).toDays();
        return numberOfDaysAhead >= 2 && numberOfDaysAhead <= 30;
    }

    @Override
    public Reservation save(Reservation reservation) {
        Instant now = Instant.now();
        if (!isValid(now, reservation))
            return null;
        reservation.setCreatedOn(now);
        reservation.setStatus(ReservationStatus.ACTIVE);
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation update(Reservation reservation) {
        Optional<Reservation> res = this.findById(reservation.getId()).map(existentReservation -> {
            Instant now = Instant.now();
            if (!isValid(now, reservation))
                return reservation;
            reservation.setCreatedOn(existentReservation.getCreatedOn());
            reservation.setUpdatedOn(now);
            reservation.setStatus(ReservationStatus.ACTIVE);
            return reservationRepository.save(reservation);
        });
        if (res.isPresent() && res.get().getUpdatedOn() != null) {
            return res.get();
        }
        return null;
    }

    @Override
    public void cancel(Reservation reservation) {
        this.findById(reservation.getId()).ifPresent(existentReservation -> {
            existentReservation.setUpdatedOn(Instant.now());
            existentReservation.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(existentReservation);
        });
    }
}
