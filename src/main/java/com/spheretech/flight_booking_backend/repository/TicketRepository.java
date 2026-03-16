package com.spheretech.flight_booking_backend.repository;

import com.spheretech.flight_booking_backend.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    // Finds a specific ticket by its unique ticket number for viewing or cancellation
    Optional<Ticket> findByTicketNumber(String ticketNumber);
}
