package com.spheretech.flight_booking_backend.service;

import com.spheretech.flight_booking_backend.dto.AirlineRequest;
import com.spheretech.flight_booking_backend.model.Airline;
import com.spheretech.flight_booking_backend.repository.AirlineRepository;
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
class AirlineServiceTest {

    @Mock
    private AirlineRepository airlineRepository;

    @InjectMocks
    private AirlineService airlineService;

    @Test
    void shouldAddAirline() {
        // Arrange
        AirlineRequest request = new AirlineRequest("Turkish Airlines");
        Airline savedAirline = new Airline(1L, "Turkish Airlines");
        
        when(airlineRepository.save(any(Airline.class))).thenReturn(savedAirline);

        // Act
        Airline result = airlineService.addAirline(request);

        // Assert
        assertNotNull(result);
        assertEquals("Turkish Airlines", result.getName());
        verify(airlineRepository, times(1)).save(any(Airline.class));
    }

    @Test
    void shouldSearchAirlines() {
        // Arrange
        Airline airline = new Airline(1L, "Turkish Airlines");
        when(airlineRepository.findByNameContainingIgnoreCase(anyString())).thenReturn(List.of(airline));

        // Act
        List<Airline> results = airlineService.searchAirlines("Turk");

        // Assert
        assertEquals(1, results.size());
        assertEquals("Turkish Airlines", results.get(0).getName());
        verify(airlineRepository, times(1)).findByNameContainingIgnoreCase("Turk");
    }
}