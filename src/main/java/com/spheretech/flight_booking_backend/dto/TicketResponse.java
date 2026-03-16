package com.spheretech.flight_booking_backend.dto;

public record TicketResponse(
    String ticketNumber,
    String passengerName,
    Long flightId,
    String maskedCardNumber,
    Double pricePaid
) {}