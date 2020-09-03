package com.newisland.reservation.model.exception;
/**
 * This Exception is throws when
 * The campsite is already reserved in the same days
 */
public class AlreadyBookedReservationException extends ReservationException{
    public AlreadyBookedReservationException(String message) {
        super(message);
    }
}
