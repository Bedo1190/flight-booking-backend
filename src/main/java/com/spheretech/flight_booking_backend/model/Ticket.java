package com.spheretech.flight_booking_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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

    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, updatable = false)
    private LocalDateTime purchaseTime;

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
        if (this.purchaseTime == null) {
            this.purchaseTime = LocalDateTime.now();
        }
    }
}