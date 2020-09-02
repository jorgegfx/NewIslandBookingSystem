package com.newisland.reservation.model.exception;
/**
 * This Exception is throws when
 * The campsite is not reserved for max 3 days.
 */
public class PassMaxRangeReservationException extends ReservationException{
    public PassMaxRangeReservationException(String message) {
        super(message);
    }
}
