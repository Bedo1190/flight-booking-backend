package com.spheretech.flight_booking_backend.service;

import com.spheretech.flight_booking_backend.dto.RouteRequest;
import com.spheretech.flight_booking_backend.exception.ResourceNotFoundException;
import com.spheretech.flight_booking_backend.model.Airport;
import com.spheretech.flight_booking_backend.model.Route;
import com.spheretech.flight_booking_backend.repository.AirportRepository;
import com.spheretech.flight_booking_backend.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private AirportRepository airportRepository;

    @InjectMocks
    private RouteService routeService;

    private Airport departure;
    private Airport arrival;

    @BeforeEach
    void setUp() {
        departure = new Airport("IST", "Istanbul Airport", "Istanbul", "Türkiye", "Europe", 41.2590, 28.7404, 0);
        // FIXED: Removed the extra parenthesis at the end of this line
        arrival = new Airport("BER", "Berlin Brandenburg", "Berlin", "Germany", "Europe", 52.3667, 13.5033, 100);
    }

    @Test
    void shouldAddRouteSuccessfully() {
        // Arrange
        // FIXED: Swapped Long IDs for 3-letter IATA codes
        RouteRequest request = new RouteRequest("IST", "BER");
        when(airportRepository.findById("IST")).thenReturn(Optional.of(departure));
        when(airportRepository.findById("BER")).thenReturn(Optional.of(arrival));

        Route savedRoute = new Route(1L, departure, arrival);
        when(routeRepository.save(any(Route.class))).thenReturn(savedRoute);

        // Act
        Route result = routeService.addRoute(request);

        // Assert
        assertNotNull(result);
        assertEquals("IST", result.getDepartureAirport().getCode());
        assertEquals("BER", result.getArrivalAirport().getCode());
        verify(routeRepository, times(1)).save(any(Route.class));
    }

    @Test
    void shouldThrowExceptionWhenDepartureAirportNotFound() {
        // Arrange
        // FIXED: Swapped Long IDs for 3-letter IATA codes
        RouteRequest request = new RouteRequest("XXX", "BER");
        when(airportRepository.findById("XXX")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> routeService.addRoute(request));
        verify(routeRepository, never()).save(any(Route.class));
    }

    @Test
    void shouldSearchRoutes() {
        // Arrange
        Route route = new Route(1L, departure, arrival);
        // FIXED: Mocked the airport lookup for the popularity score increment
        when(airportRepository.findById("BER")).thenReturn(Optional.of(arrival)); 
        // FIXED: The repository method name now uses "Code" instead of "Id", and takes Strings
        when(routeRepository.findByDepartureAirportCodeAndArrivalAirportCode("IST", "BER")).thenReturn(List.of(route));

        // Act
        // FIXED: Method signature expects Strings
        List<Route> results = routeService.searchRoutes("IST", "BER");

        // Assert
        assertEquals(1, results.size());
        verify(routeRepository, times(1)).findByDepartureAirportCodeAndArrivalAirportCode("IST", "BER");
    }
}