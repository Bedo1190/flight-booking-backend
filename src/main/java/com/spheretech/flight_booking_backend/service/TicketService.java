package com.spheretech.flight_booking_backend.service;

import com.spheretech.flight_booking_backend.dto.FraudCheckRequest;
import com.spheretech.flight_booking_backend.dto.FraudCheckResponse;
import com.spheretech.flight_booking_backend.dto.TicketPurchaseRequest;
import com.spheretech.flight_booking_backend.dto.TicketResponse;
import com.spheretech.flight_booking_backend.exception.FlightFullException;
import com.spheretech.flight_booking_backend.exception.FraudDetectedException;
import com.spheretech.flight_booking_backend.exception.ResourceNotFoundException;
import com.spheretech.flight_booking_backend.model.Airport;
import com.spheretech.flight_booking_backend.model.Flight;
import com.spheretech.flight_booking_backend.model.Ticket;
import com.spheretech.flight_booking_backend.repository.AirportRepository;
import com.spheretech.flight_booking_backend.repository.FlightRepository;
import com.spheretech.flight_booking_backend.repository.TicketRepository;
import com.spheretech.flight_booking_backend.util.CardUtils;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

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

        String maskedCard = CardUtils.maskCardNumber(request.cardNumber());

        // Velocity Calculation logic
        int hoursSinceLastTxn = 8760; 
        Double lastAmount = 0.0;

        Optional<Ticket> lastTicketOpt = ticketRepository.findFirstByMaskedCardNumberOrderByPurchaseTimeDesc(maskedCard);
        if (lastTicketOpt.isPresent()) {
            Ticket lastTicket = lastTicketOpt.get();
            lastAmount = lastTicket.getPricePaid();
            hoursSinceLastTxn = (int) ChronoUnit.HOURS.between(lastTicket.getPurchaseTime(), LocalDateTime.now());
        }

        Double currentPrice = flight.getCurrentPrice();
        int daysUntilFlight = (int) ChronoUnit.DAYS.between(LocalDateTime.now(), flight.getDepartureTime());
        int timeOfDay = LocalDateTime.now().getHour();

        try {
            FraudCheckRequest aiRequest = new FraudCheckRequest(
                currentPrice, 
                timeOfDay, 
                Math.max(0, daysUntilFlight), 
                hoursSinceLastTxn, 
                lastAmount
            );

            System.out.println("DEBUG - Calling AI with: " + aiRequest);

            // MODIFIED: Explicitly setting content type and using the request object
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<FraudCheckRequest> entity = new HttpEntity<>(aiRequest, headers);

            ResponseEntity<FraudCheckResponse> response = restTemplate.postForEntity(
                "http://localhost:8000/fraud-check",
                entity,
                FraudCheckResponse.class
                );

            FraudCheckResponse fraudResponse = response.getBody();

            if (fraudResponse != null && fraudResponse.is_fraud()) {
                throw new FraudDetectedException("Blocked by AI. Risk score: " + fraudResponse.fraud_probability());
            }

        } catch (FraudDetectedException e) {
            throw e; // Re-throw the fraud exception so it's not caught by the generic catch below
        } catch (Exception e) {
            System.err.println("Fraud AI Communication Error: " + e.getMessage());
            // Fail open: continue transaction if AI is down
        }

        Ticket ticket = new Ticket();
        ticket.setFlight(flight);
        ticket.setFirstName(request.firstName());
        ticket.setLastName(request.lastName());
        ticket.setMaskedCardNumber(maskedCard);
        ticket.setPricePaid(currentPrice);
        ticket.setPurchaseTime(LocalDateTime.now());

        // MODIFIED: saveAndFlush to ensure visibility for the next immediate request
        Ticket savedTicket = ticketRepository.saveAndFlush(ticket); 

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
                ticket.getPricePaid(),
                ticket.getPurchaseTime()
        );
    }
}