package com.spheretech.flight_booking_backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Airline airline;

    @Version
    private Long version;

    @ManyToOne
    private Route route;

    private Double basePrice;
    private Integer totalCapacity;
    private Integer occupiedSeats = 0;
    private LocalDateTime departureTime;

    public Double getCurrentPrice() {
        if (totalCapacity == null || totalCapacity == 0) {
            return basePrice;
        }
        
        // Logic: 10% increase for every 10% occupancy
        double occupancyRate = (double) occupiedSeats / totalCapacity;
        int priceIncrements = (int) (occupancyRate * 10);
        return basePrice * Math.pow(1.10, priceIncrements); 
    }
}
