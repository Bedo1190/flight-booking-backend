package com.spheretech.flight_booking_backend.dto;

import jakarta.validation.constraints.NotBlank;

public record AirportRequest(
    @NotBlank(message = "Airport name is required") 
    String name,
    
    @NotBlank(message = "City is required") 
    String city,
    
    @NotBlank(message = "Airport code is required") 
    String code
) {}