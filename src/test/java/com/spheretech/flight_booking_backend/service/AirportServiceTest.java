package com.spheretech.flight_booking_backend.service;

import com.spheretech.flight_booking_backend.dto.AirportRequest;
import com.spheretech.flight_booking_backend.model.Airport;
import com.spheretech.flight_booking_backend.repository.AirportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AirportServiceTest {

    @Mock
    private AirportRepository airportRepository;

    @InjectMocks
    private AirportService airportService;

    @Test
    void shouldAddAirport() {
        // Arrange
        AirportRequest request = new AirportRequest("Istanbul Airport", "Istanbul", "IST", "Türkiye","Europe", 41.2590, 28.7404);
        Airport savedAirport = new Airport("IST", "Istanbul Airport","Istanbul", "Türkiye", "Europe", 41.2590, 28.7404, 0);

        when(airportRepository.save(any(Airport.class))).thenReturn(savedAirport);

        // Act
        Airport result = airportService.addAirport(request);

        // Assert
        assertNotNull(result);
        assertEquals("IST", result.getCode());
        verify(airportRepository, times(1)).save(any(Airport.class));
    }

    @Test
    void shouldSearchAirports() {
        // Arrange
        Airport airport = new Airport("IST", "Istanbul Airport","Istanbul", "Türkiye", "Europe", 41.2590, 28.7404, 0);
        when(airportRepository.findByNameContainingIgnoreCase(anyString())).thenReturn(List.of(airport));

        // Act
        List<Airport> results = airportService.searchAirports("Istan");

        // Assert
        assertEquals(1, results.size());
        assertEquals("Istanbul Airport", results.get(0).getName());
        verify(airportRepository, times(1)).findByNameContainingIgnoreCase("Istan");
    }
}