package com.spheretech.flight_booking_backend.dto;

public record FraudCheckResponse(
    Double fraud_probability,
    Boolean is_fraud
) {}