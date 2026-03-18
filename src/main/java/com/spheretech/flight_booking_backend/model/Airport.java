package com.spheretech.flight_booking_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Airport {
    @Id
    @Column(length = 3, updatable = false, nullable = false)
    private String code; // e.g., "IST", "FRA", "BER"
    private String name;
    private String city;
    private String country;
    
    // ML Feature: Categorical grouping
    private String region; 
    
    // ML Feature: Spatial coordinates
    @Column(nullable = false)
    private Double latitude;
    
    @Column(nullable = false)
    private Double longitude;

    // ML Feature: Cold-start solver
    @Column(columnDefinition = "integer default 0")
    private Integer popularityScore = 0;
}