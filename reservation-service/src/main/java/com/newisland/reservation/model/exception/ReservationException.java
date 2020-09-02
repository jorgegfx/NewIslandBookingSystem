package com.newisland.reservation.model.exception;
/**
 * Base Reservation Exception
 */
public abstract class ReservationException extends RuntimeException{
    public ReservationException(String message) {
        super(message);
    }
}
