package com.spheretech.flight_booking_backend.service;

import com.spheretech.flight_booking_backend.dto.TicketPurchaseRequest;
import com.spheretech.flight_booking_backend.dto.TicketResponse;
import com.spheretech.flight_booking_backend.exception.FlightFullException;
import com.spheretech.flight_booking_backend.exception.ResourceNotFoundException;
import com.spheretech.flight_booking_backend.model.Airport;
import com.spheretech.flight_booking_backend.model.Flight;
import com.spheretech.flight_booking_backend.model.Ticket;
import com.spheretech.flight_booking_backend.repository.AirportRepository;
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
    private final InteractionService interactionService;
    private final AirportRepository airportRepository;

    @Transactional
    public TicketResponse purchaseTicket(TicketPurchaseRequest request) {
        Flight flight = flightRepository.findById(request.flightId())
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with ID: " + request.flightId()));

        if (flight.getOccupiedSeats() >= flight.getTotalCapacity()) {
            throw new FlightFullException("Flight " + flight.getId() + " is currently at maximum capacity.");
        }

        Ticket ticket = new Ticket();
        ticket.setFlight(flight);
        ticket.setFirstName(request.firstName());
        ticket.setLastName(request.lastName());
        ticket.setMaskedCardNumber(CardUtils.maskCardNumber(request.cardNumber()));
        ticket.setPricePaid(flight.getCurrentPrice());

        Ticket savedTicket = ticketRepository.save(ticket);

        flight.setOccupiedSeats(flight.getOccupiedSeats() + 1);
        flightRepository.save(flight);

        Airport arrivalAirport = flight.getRoute().getArrivalAirport();
        arrivalAirport.setPopularityScore(arrivalAirport.getPopularityScore() + 2);
        airportRepository.save(arrivalAirport);
        
        interactionService.logInteraction(request.passengerId(), flight.getId(), "PURCHASE");

        return mapToResponse(savedTicket);
    }

    public TicketResponse getSecuredTicket(String ticketNumber, String lastName) {
        Ticket ticket = ticketRepository.findByTicketNumberAndLastNameIgnoreCase(ticketNumber, lastName)
                .orElseThrow(() -> new ResourceNotFoundException("No ticket found matching that number and last name."));
        
        return mapToResponse(ticket);
    }

    @Transactional
    public void cancelSecuredTicket(String ticketNumber, String lastName) {
         Ticket ticket = ticketRepository.findByTicketNumberAndLastNameIgnoreCase(ticketNumber, lastName)
                .orElseThrow(() -> new ResourceNotFoundException("No ticket found matching that number and last name."));

         Flight flight = ticket.getFlight();
         
         if(flight.getOccupiedSeats() > 0) {
             flight.setOccupiedSeats(flight.getOccupiedSeats() - 1);
             flightRepository.save(flight);
         }
         
         ticketRepository.delete(ticket);
    }

    // Helper method to keep your code DRY (Don't Repeat Yourself)
    private TicketResponse mapToResponse(Ticket ticket) {
        Flight flight = ticket.getFlight();
        return new TicketResponse(
                ticket.getTicketNumber(),
                flight.getId(),
                ticket.getFirstName(),
                ticket.getLastName(),
                flight.getRoute().getDepartureAirport().getCode(),
                flight.getRoute().getDepartureAirport().getCity(),
                flight.getRoute().getArrivalAirport().getCode(),
                flight.getRoute().getArrivalAirport().getCity(),
                flight.getDepartureTime(),
                ticket.getMaskedCardNumber(),
                ticket.getPricePaid()
        );
    }
}