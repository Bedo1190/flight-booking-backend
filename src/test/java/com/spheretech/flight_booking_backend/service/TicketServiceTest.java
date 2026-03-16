package com.spheretech.flight_booking_backend.service;

import com.spheretech.flight_booking_backend.dto.TicketPurchaseRequest;
import com.spheretech.flight_booking_backend.dto.TicketResponse;
import com.spheretech.flight_booking_backend.exception.FlightFullException;
import com.spheretech.flight_booking_backend.exception.ResourceNotFoundException;
import com.spheretech.flight_booking_backend.model.Flight;
import com.spheretech.flight_booking_backend.model.Ticket;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private TicketService ticketService;

    private Flight testFlight;

    @BeforeEach
    void setUp() {
        testFlight = new Flight();
        testFlight.setId(1L);
        testFlight.setBasePrice(100.0);
        testFlight.setTotalCapacity(50);
        testFlight.setOccupiedSeats(10); // 20% full -> price should be 121.0
    }

    @Test
    void shouldSuccessfullyPurchaseTicket() {
        // Arrange
        TicketPurchaseRequest request = new TicketPurchaseRequest(1L, "John Doe", "4221161122330005");
        
        when(flightRepository.findById(1L)).thenReturn(Optional.of(testFlight));
        
        Ticket savedTicket = new Ticket();
        savedTicket.setTicketNumber("TK-12345");
        savedTicket.setPassengerName("John Doe");
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
        
        verify(flightRepository, times(1)).save(testFlight);
    }

    @Test
    void shouldThrowExceptionWhenFlightNotFound() {
        // Arrange
        TicketPurchaseRequest request = new TicketPurchaseRequest(99L, "John Doe", "4221161122330005");
        when(flightRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> ticketService.purchaseTicket(request));
    }

    @Test
    void shouldThrowExceptionWhenFlightIsFull() {
        // Arrange
        testFlight.setOccupiedSeats(50); // Flight is at max capacity
        TicketPurchaseRequest request = new TicketPurchaseRequest(1L, "John Doe", "4221161122330005");
        when(flightRepository.findById(1L)).thenReturn(Optional.of(testFlight));

        // Act & Assert
        assertThrows(FlightFullException.class, () -> ticketService.purchaseTicket(request));
    }
}