package com.spheretech.flight_booking_backend.exception;

public class FlightFullException extends RuntimeException {
    public FlightFullException(String message) {
        super(message);
    }
}