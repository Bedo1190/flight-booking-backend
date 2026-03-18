package com.spheretech.flight_booking_backend.repository;

import com.spheretech.flight_booking_backend.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    // SECURED: Requires both the ticket number and the passenger's last name
    Optional<Ticket> findByTicketNumberAndLastNameIgnoreCase(String ticketNumber, String lastName);
}