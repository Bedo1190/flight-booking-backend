package com.spheretech.flight_booking_backend.service;

import com.spheretech.flight_booking_backend.dto.FlightRequest;
import com.spheretech.flight_booking_backend.exception.ResourceNotFoundException;
import com.spheretech.flight_booking_backend.model.Airline;
import com.spheretech.flight_booking_backend.model.Flight;
import com.spheretech.flight_booking_backend.model.Route;
import com.spheretech.flight_booking_backend.repository.AirlineRepository;
import com.spheretech.flight_booking_backend.repository.FlightRepository;
import com.spheretech.flight_booking_backend.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private AirlineRepository airlineRepository;

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private FlightService flightService;

    private Airline airline;
    private Route route;

    @BeforeEach
    void setUp() {
        airline = new Airline(1L, "Turkish Airlines");
        route = new Route(1L, null, null); // Simplified for this test
    }

    @Test
    void shouldAddFlightSuccessfully() {
        // Arrange
        FlightRequest request = new FlightRequest(1L, 1L, 100.0, 150, LocalDateTime.now().plusDays(1));
        
        when(airlineRepository.findById(1L)).thenReturn(Optional.of(airline));
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));

        Flight savedFlight = new Flight(1L, airline, route, 100.0, 150, 0, request.departureTime());
        when(flightRepository.save(any(Flight.class))).thenReturn(savedFlight);

        // Act
        Flight result = flightService.addFlight(request);

        // Assert
        assertNotNull(result);
        assertEquals(150, result.getTotalCapacity());
        assertEquals(0, result.getOccupiedSeats()); // Ensure it defaults to 0
        verify(flightRepository, times(1)).save(any(Flight.class));
    }

    @Test
    void shouldThrowExceptionWhenAirlineNotFound() {
        // Arrange
        FlightRequest request = new FlightRequest(99L, 1L, 100.0, 150, LocalDateTime.now().plusDays(1));
        when(airlineRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> flightService.addFlight(request));
        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    void shouldSearchFlightsByAirline() {
        // Arrange
        Flight flight = new Flight(1L, airline, route, 100.0, 150, 0, LocalDateTime.now().plusDays(1));
        when(flightRepository.findByAirlineId(1L)).thenReturn(List.of(flight));

        // Act
        List<Flight> results = flightService.searchFlightsByAirline(1L);

        // Assert
        assertEquals(1, results.size());
        assertEquals("Turkish Airlines", results.get(0).getAirline().getName());
        verify(flightRepository, times(1)).findByAirlineId(1L);
    }
}