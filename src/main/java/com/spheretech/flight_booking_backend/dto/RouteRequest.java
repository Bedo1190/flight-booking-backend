package com.spheretech.flight_booking_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RouteRequest(
    @NotBlank(message = "Departure airport code is required")
    @Size(min = 3, max = 3, message = "Airport code must be exactly 3 characters")
    String departureAirportCode,

    @NotBlank(message = "Arrival airport code is required")
    @Size(min = 3, max = 3, message = "Airport code must be exactly 3 characters")
    String arrivalAirportCode
) {}