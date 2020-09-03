package com.newisland.reservation.model.exception;
/**
 * This Exception is throws when
 * The campsite is not reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance.
 */
public class AheadDaysReservationException extends ReservationException{
    public AheadDaysReservationException(String message) {
        super(message);
    }
}
