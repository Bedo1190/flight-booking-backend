package com.spheretech.flight_booking_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Anonymous tracking ID for the user (We will use an email or UUID here)
    @Column(nullable = false)
    private String passengerId; 

    @Column(nullable = false)
    private Long flightId;   

    // Will be either "SEARCH" or "PURCHASE"
    @Column(nullable = false)
    private String interactionType; 

    private LocalDateTime timestamp;
    
    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }
}