package com.spheretech.flight_booking_backend.dto;

import jakarta.validation.constraints.NotBlank;

public record AirlineRequest(
    @NotBlank(message = "Airline name is required")
    String name
) {}