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
        departure = new Airport(1L, "Istanbul Airport", "Istanbul", "IST");
        arrival = new Airport(2L, "Berlin Brandenburg", "Berlin", "BER");
    }

    @Test
    void shouldAddRouteSuccessfully() {
        // Arrange
        RouteRequest request = new RouteRequest(1L, 2L);
        when(airportRepository.findById(1L)).thenReturn(Optional.of(departure));
        when(airportRepository.findById(2L)).thenReturn(Optional.of(arrival));

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
        RouteRequest request = new RouteRequest(99L, 2L);
        when(airportRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> routeService.addRoute(request));
        verify(routeRepository, never()).save(any(Route.class));
    }

    @Test
    void shouldSearchRoutes() {
        // Arrange
        Route route = new Route(1L, departure, arrival);
        when(routeRepository.findByDepartureAirportIdAndArrivalAirportId(1L, 2L)).thenReturn(List.of(route));

        // Act
        List<Route> results = routeService.searchRoutes(1L, 2L);

        // Assert
        assertEquals(1, results.size());
        verify(routeRepository, times(1)).findByDepartureAirportIdAndArrivalAirportId(1L, 2L);
    }
}