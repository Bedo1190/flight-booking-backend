package com.spheretech.flight_booking_backend.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record FlightRequest(
    @NotNull(message = "Airline ID is required")
    Long airlineId,
    
    @NotNull(message = "Route ID is required")
    Long routeId,
    
    @NotNull(message = "Base price is required")
    @Min(value = 0, message = "Price must be positive")
    Double basePrice,
    
    @NotNull(message = "Total capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    Integer totalCapacity,
    
    @NotNull(message = "Departure time is required")
    @Future(message = "Departure time must be in the future")
    LocalDateTime departureTime
) {}