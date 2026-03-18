package com.spheretech.flight_booking_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FraudCheckRequest(
    @JsonProperty("ticket_price") Double ticket_price,
    @JsonProperty("time_of_day") Integer time_of_day,
    @JsonProperty("days_until_flight") Integer days_until_flight,
    @JsonProperty("hours_since_last_txn") Integer hours_since_last_txn,
    @JsonProperty("last_txn_amount") Double last_txn_amount
) {}