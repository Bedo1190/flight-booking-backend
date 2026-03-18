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

    @Column(unique = true, nullable = false)
    private String ticketNumber; 

    // Split for security validation
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;

    @ManyToOne
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @Column(nullable = false)
    private String maskedCardNumber; 

    @Column(nullable = false)
    private Double pricePaid; 

    @PrePersist
    protected void onCreate() {
        if (this.ticketNumber == null) {
            this.ticketNumber = "TK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }
}