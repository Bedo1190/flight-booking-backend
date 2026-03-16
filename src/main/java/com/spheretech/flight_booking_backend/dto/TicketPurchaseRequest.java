package com.spheretech.flight_booking_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record TicketPurchaseRequest(
    @NotNull(message = "Flight ID cannot be null") 
    Long flightId,
    
    @NotBlank(message = "Passenger name is required") 
    String passengerName,
    
    @NotBlank(message = "Credit card number is required")
    // A basic regex to ensure the card has at least enough digits to be masked
    @Pattern(regexp = ".*\\d{10,}.*", message = "Invalid credit card format")
    String cardNumber 
) {}