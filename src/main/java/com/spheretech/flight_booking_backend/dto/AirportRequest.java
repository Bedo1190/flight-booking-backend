package com.spheretech.flight_booking_backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AirportRequest(
    @NotBlank(message = "Airport name is required") 
    String name,
    
    @NotBlank(message = "City is required") 
    String city,
    
    @NotBlank(message = "Airport code is required") 
    String code,

    @NotBlank(message = "Country is required")
    String country,

    @NotBlank(message = "Region is required for recommendations")
    String region,

    @NotNull(message = "Latitude is required")
    @Min(value = -90, message = "Latitude must be between -90 and 90")
    @Max(value = 90, message = "Latitude must be between -90 and 90")
    Double latitude,

    @NotNull(message = "Longitude is required")
    @Min(value = -180, message = "Longitude must be between -180 and 180")
    @Max(value = 180, message = "Longitude must be between -180 and 180")
    Double longitude
) {}