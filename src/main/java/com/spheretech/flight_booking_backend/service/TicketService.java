package com.spheretech.flight_booking_backend.service;

import com.spheretech.flight_booking_backend.dto.TicketPurchaseRequest;
import com.spheretech.flight_booking_backend.dto.TicketResponse;
import com.spheretech.flight_booking_backend.exception.FlightFullException;
import com.spheretech.flight_booking_backend.exception.ResourceNotFoundException;
import com.spheretech.flight_booking_backend.model.Flight;
import com.spheretech.flight_booking_backend.model.Ticket;
import com.spheretech.flight_booking_backend.repository.FlightRepository;
import com.spheretech.flight_booking_backend.repository.TicketRepository;
import com.spheretech.flight_booking_backend.util.CardUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final FlightRepository flightRepository;

    @Transactional
    public TicketResponse purchaseTicket(TicketPurchaseRequest request) {
        // 1. Retrieve the flight
        Flight flight = flightRepository.findById(request.flightId())
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with ID: " + request.flightId()));

        // 2. Check capacity
        if (flight.getOccupiedSeats() >= flight.getTotalCapacity()) {
            throw new FlightFullException("Flight " + flight.getId() + " is currently at maximum capacity.");
        }

        // 3. Calculate current price based on occupancy
        Double currentPrice = flight.getCurrentPrice();

        // 4. Create and populate the Ticket entity
        Ticket ticket = new Ticket();
        ticket.setFlight(flight);
        ticket.setPassengerName(request.passengerName());
        ticket.setMaskedCardNumber(CardUtils.maskCardNumber(request.cardNumber()));
        ticket.setPricePaid(currentPrice);

        // 5. Save the ticket (this triggers the @PrePersist to generate the ticketNumber)
        Ticket savedTicket = ticketRepository.save(ticket);

        // 6. Update flight occupancy
        flight.setOccupiedSeats(flight.getOccupiedSeats() + 1);
        flightRepository.save(flight);

        // 7. Return the response DTO
        return new TicketResponse(
                savedTicket.getTicketNumber(),
                savedTicket.getPassengerName(),
                savedTicket.getFlight().getId(),
                savedTicket.getMaskedCardNumber(),
                savedTicket.getPricePaid()
        );
    }

    public TicketResponse getTicketByNumber(String ticketNumber) {
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with number: " + ticketNumber));
        
        return new TicketResponse(
                ticket.getTicketNumber(),
                ticket.getPassengerName(),
                ticket.getFlight().getId(),
                ticket.getMaskedCardNumber(),
                ticket.getPricePaid()
        );
    }

    @Transactional
    public void cancelTicket(String ticketNumber) {
         Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with number: " + ticketNumber));

         Flight flight = ticket.getFlight();
         
         // Decrease occupancy
         if(flight.getOccupiedSeats() > 0) {
             flight.setOccupiedSeats(flight.getOccupiedSeats() - 1);
             flightRepository.save(flight);
         }
         
         ticketRepository.delete(ticket);
    }
}