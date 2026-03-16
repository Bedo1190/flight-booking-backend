package com.spheretech.flight_booking_backend.dto;

import jakarta.validation.constraints.NotNull;

public record RouteRequest(
    @NotNull(message = "Departure Airport ID is required")
    Long departureAirportId,
    
    @NotNull(message = "Arrival Airport ID is required")
    Long arrivalAirportId
) {}