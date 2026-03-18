package com.spheretech.flight_booking_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record TicketPurchaseRequest(
    @NotNull(message = "Flight ID cannot be null") 
    Long flightId,
    
    @NotBlank(message = "First name is required") 
    String firstName,

    @NotBlank(message = "Last name is required") 
    String lastName,

    @NotBlank(message = "Passenger ID (Email) is required")
    @Email(message = "Passenger ID must be a valid email format")
    String passengerId,
    
    @NotBlank(message = "Credit card number is required")
    @Pattern(regexp = ".*\\d{10,}.*", message = "Invalid credit card format")
    String cardNumber 
) {}