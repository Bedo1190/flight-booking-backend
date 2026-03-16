package com.spheretech.flight_booking_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // A unique identifier generated at purchase for search and cancellation 
    @Column(unique = true, nullable = false)
    private String ticketNumber; 

    @Column(nullable = false)
    private String passengerName;

    @ManyToOne
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    // Storing ONLY the masked version for security 
    @Column(nullable = false)
    private String maskedCardNumber; 

    // Locks in the price the user actually paid at the moment of booking
    @Column(nullable = false)
    private Double pricePaid; 

    // Helper method to automatically generate a random ticket number before saving
    @PrePersist
    protected void onCreate() {
        if (this.ticketNumber == null) {
            // Generates a short, unique alphanumeric ticket number (e.g., "TK-8f9a2b")
            this.ticketNumber = "TK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }
}