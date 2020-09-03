package com.newisland.reservation.service;

import com.newisland.reservation.model.ValidationResult;
import com.newisland.reservation.model.entity.Reservation;
import com.newisland.reservation.model.entity.ReservationStatus;
import com.newisland.reservation.model.exception.AheadDaysReservationException;
import com.newisland.reservation.model.exception.AlreadyBookedReservationException;
import com.newisland.reservation.model.exception.PassMaxRangeReservationException;
import com.newisland.reservation.model.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${max.allowed:3}")
    private int maxAllowedDays;

    @Value("${min.allowed.ahead:1}")
    private int minAllowedDaysAhead;

    @Value("${max.allowed.ahead:30}")
    private int maxAllowedDaysAhead;

    public ReservationServiceImpl() {
    }

    public ReservationServiceImpl(int maxAllowedDays,
                                  int minAllowedDaysAhead,
                                  int maxAllowedDaysAhead,
                                  ReservationRepository reservationRepository) {
        this.maxAllowedDays = maxAllowedDays;
        this.minAllowedDaysAhead = minAllowedDaysAhead;
        this.maxAllowedDaysAhead = maxAllowedDaysAhead;
        this.reservationRepository = reservationRepository;
    }

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

    @Override
    public boolean isAvailable(UUID id, UUID campsiteId, Instant start, Instant end) {
        return reservationRepository.countAvailabilityForExistingRecord(id,campsiteId, ReservationStatus.ACTIVE, start, end) == 0;
    }

    private ValidationResult validate(Instant now, Reservation reservation,Optional<Reservation> existingRecord) {
        long numberOfDaysToReserve = Duration.between(
                reservation.getArrivalDate(),
                reservation.getDepartureDate()).toDays();
        //The campsite can be reserved for max 3 days.
        if (numberOfDaysToReserve > maxAllowedDays)
            return ValidationResult.PASS_MAX_RANGE_NON_VALID;

        //The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance.
        long numberOfDaysAhead = Duration.between(
                now,reservation.getArrivalDate()).toDays();

        if(now.isAfter(reservation.getArrivalDate()) ||
                numberOfDaysAhead < minAllowedDaysAhead ||
                    numberOfDaysAhead > maxAllowedDaysAhead)
            return ValidationResult.AHEAD_MAX_RANGE_NON_VALID;

        //Reservation is not Double Booked
        if(existingRecord.isPresent()){
            if (!isAvailable(existingRecord.get().getId(),
                    reservation.getCampsiteId(),
                    reservation.getArrivalDate(),
                    reservation.getDepartureDate()))
                return ValidationResult.ALREADY_BOOKED_NON_VALID;
        }else{
            if (!isAvailable(reservation.getCampsiteId(),
                    reservation.getArrivalDate(),
                    reservation.getDepartureDate()))
                return ValidationResult.ALREADY_BOOKED_NON_VALID;
        }
        return ValidationResult.VALID;
    }

    private Reservation trySaveRecord(Instant now,Reservation reservation, Optional<Reservation> existingRecord){
        switch (validate(now, reservation,existingRecord)){
            case VALID: reservation.setStatus(ReservationStatus.ACTIVE);
                break;
            case ALREADY_BOOKED_NON_VALID:
                throw new AlreadyBookedReservationException(
                        String.format("Invalid Reservation %s : Campsite is already reserved!",reservation));
            case PASS_MAX_RANGE_NON_VALID:
                throw new PassMaxRangeReservationException(
                        String.format("The Reservation %s is passing the maximum allowed (%d) days",
                                reservation,maxAllowedDays));
            case AHEAD_MAX_RANGE_NON_VALID:
                throw new AheadDaysReservationException(String.format(
                        "The Reservation %s is passing the min(%d) or max(%d) allowed days ahead to booked",
                        reservation,minAllowedDaysAhead,maxAllowedDaysAhead));
        }
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation save(Reservation reservation) {
        Instant now = Instant.now();
        reservation.setCreatedOn(now);
        return trySaveRecord(now,reservation,Optional.empty());
    }

    @Override
    public Optional<Reservation> update(Reservation reservation) {
        Optional<Reservation> res = this.findById(reservation.getId()).map(existentReservation -> {
            Instant now = Instant.now();
            reservation.setUserId(existentReservation.getUserId());
            reservation.setCreatedOn(existentReservation.getCreatedOn());
            reservation.setUpdatedOn(now);
            return trySaveRecord(now,reservation,Optional.of(existentReservation));
        });
        if (res.isPresent() && res.get().getUpdatedOn() != null) {
            return res;
        }
        return Optional.empty();
    }

    @Override
    public Optional<Reservation> cancel(UUID id) {
        return this.findById(id).map(existentReservation -> {
            existentReservation.setUpdatedOn(Instant.now());
            existentReservation.setStatus(ReservationStatus.CANCELLED);
            return reservationRepository.save(existentReservation);
        });
    }
}
