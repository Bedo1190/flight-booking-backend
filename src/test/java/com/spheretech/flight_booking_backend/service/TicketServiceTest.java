package com.spheretech.flight_booking_backend.service;

import com.spheretech.flight_booking_backend.dto.TicketPurchaseRequest;
import com.spheretech.flight_booking_backend.dto.TicketResponse;
import com.spheretech.flight_booking_backend.exception.FlightFullException;
import com.spheretech.flight_booking_backend.exception.ResourceNotFoundException;
import com.spheretech.flight_booking_backend.model.Airport;
import com.spheretech.flight_booking_backend.model.Flight;
import com.spheretech.flight_booking_backend.model.Route;
import com.spheretech.flight_booking_backend.model.Ticket;
import com.spheretech.flight_booking_backend.repository.AirportRepository;
import com.spheretech.flight_booking_backend.repository.FlightRepository;
import com.spheretech.flight_booking_backend.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private AirportRepository airportRepository;

    @Mock
    private InteractionService interactionService;

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private TicketService ticketService;

    private Flight testFlight;

    @BeforeEach
    void setUp() {
        // 1. Setup Dummy Airports to prevent NullPointerExceptions
        Airport departure = new Airport();
        departure.setCode("IST");
        departure.setCity("Istanbul");

        Airport arrival = new Airport();
        arrival.setCode("BER");
        arrival.setCity("Berlin");
        arrival.setPopularityScore(0); 

        // 2. Setup Dummy Route
        Route route = new Route();
        route.setDepartureAirport(departure);
        route.setArrivalAirport(arrival);

        // 3. Setup Dummy Flight and attach the Route
        testFlight = new Flight();
        testFlight.setId(1L);
        testFlight.setBasePrice(100.0);
        testFlight.setTotalCapacity(50);
        testFlight.setOccupiedSeats(10); // 20% full -> price should be 121.0
        testFlight.setRoute(route); // <-- This is the crucial fix!
    }

    @Test
    void shouldSuccessfullyPurchaseTicket() {
        // Arrange
        TicketPurchaseRequest request = new TicketPurchaseRequest(1L, "John", "Doe", "john@example.com", "4221161122330005");
        
        when(flightRepository.findById(1L)).thenReturn(Optional.of(testFlight));
        
        Ticket savedTicket = new Ticket();
        savedTicket.setTicketNumber("TK-12345");
        savedTicket.setFirstName("John");
        savedTicket.setLastName("Doe");
        savedTicket.setFlight(testFlight);
        savedTicket.setMaskedCardNumber("422116******0005");
        savedTicket.setPricePaid(121.0);
        
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        // Act
        TicketResponse response = ticketService.purchaseTicket(request);

        // Assert
        assertNotNull(response);
        assertEquals("TK-12345", response.ticketNumber());
        assertEquals("422116******0005", response.maskedCardNumber());
        assertEquals(121.0, response.pricePaid());
        assertEquals(11, testFlight.getOccupiedSeats()); // Occupancy should increase
        
        // Verify the flight and the AI popularity score updates were triggered
        verify(flightRepository, times(1)).save(testFlight);
        verify(airportRepository, times(1)).save(any(Airport.class));
        verify(interactionService, times(1)).logInteraction(anyString(), anyLong(), anyString());
    }

    @Test
    void shouldThrowExceptionWhenFlightNotFound() {
        // Arrange 
        TicketPurchaseRequest request = new TicketPurchaseRequest(99L, "John", "Doe", "john@example.com", "4221161122330005");
        
        when(flightRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> ticketService.purchaseTicket(request));
    }

    @Test
    void shouldThrowExceptionWhenFlightIsFull() {
        // Arrange
        testFlight.setOccupiedSeats(50); // Flight is at max capacity
        TicketPurchaseRequest request = new TicketPurchaseRequest(1L, "John", "Doe", "john@example.com", "4221161122330005");
        when(flightRepository.findById(1L)).thenReturn(Optional.of(testFlight));

        // Act & Assert
        assertThrows(FlightFullException.class, () -> ticketService.purchaseTicket(request));
    }
    
    @Test
    void shouldGetSecuredTicket() {
        // Arrange
        Ticket ticket = new Ticket();
        ticket.setTicketNumber("TK-12345");
        ticket.setFirstName("John");
        ticket.setLastName("Doe");
        ticket.setFlight(testFlight);
        ticket.setMaskedCardNumber("422116******0005");
        ticket.setPricePaid(121.0);

        when(ticketRepository.findByTicketNumberAndLastNameIgnoreCase("TK-12345", "Doe"))
                .thenReturn(Optional.of(ticket));

        // Act
        TicketResponse response = ticketService.getSecuredTicket("TK-12345", "Doe");

        // Assert
        assertNotNull(response);
        assertEquals("TK-12345", response.ticketNumber());
        assertEquals("Doe", response.lastName());
    }

    @Test
    void shouldCancelSecuredTicket() {
        // Arrange
        Ticket ticket = new Ticket();
        ticket.setTicketNumber("TK-12345");
        ticket.setLastName("Doe");
        testFlight.setOccupiedSeats(10);
        ticket.setFlight(testFlight);

        when(ticketRepository.findByTicketNumberAndLastNameIgnoreCase("TK-12345", "Doe"))
                .thenReturn(Optional.of(ticket));

        // Act
        ticketService.cancelSecuredTicket("TK-12345", "Doe");

        // Assert
        assertEquals(9, testFlight.getOccupiedSeats()); // Occupancy should decrease
        verify(flightRepository, times(1)).save(testFlight);
        verify(ticketRepository, times(1)).delete(ticket);
    }

    @Test
    void shouldThrowExceptionWhenTicketNotFoundForSecurity() {
        // Arrange
        when(ticketRepository.findByTicketNumberAndLastNameIgnoreCase("TK-999", "Hacker"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
                () -> ticketService.getSecuredTicket("TK-999", "Hacker"));
    }
}