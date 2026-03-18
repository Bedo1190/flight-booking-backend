package com.spheretech.flight_booking_backend.dto;

import java.time.LocalDateTime;

public record TicketResponse(
    String ticketNumber,
    Long flightId,
    String firstName,
    String lastName,
    String departureAirportCode,
    String departureCity,
    String arrivalAirportCode,
    String arrivalCity,
    LocalDateTime departureTime,
    String maskedCardNumber,
    Double pricePaid,
    LocalDateTime purchaseTime
) {}