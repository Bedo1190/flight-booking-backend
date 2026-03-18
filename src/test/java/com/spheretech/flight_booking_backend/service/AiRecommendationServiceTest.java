package com.spheretech.flight_booking_backend.service;

import com.spheretech.flight_booking_backend.model.Flight;
import com.spheretech.flight_booking_backend.repository.FlightRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiRecommendationServiceTest {

    @Mock private RestClient aiRestClient;
    @Mock private FlightRepository flightRepository;
    @InjectMocks private AiRecommendationService recommendationService;

    @Mock private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock private RestClient.RequestHeadersSpec requestHeadersSpec;
    @Mock private RestClient.ResponseSpec responseSpec;

    @Test
    void shouldReturnAiRecommendations() {
        // Arrange
        when(aiRestClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(List.of(1L, 2L));

        Flight f1 = new Flight(); f1.setId(1L);
        Flight f2 = new Flight(); f2.setId(2L);
        when(flightRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(f1, f2));

        // Act
        List<Flight> results = recommendationService.getRecommendations("user123");

        // Assert
        assertEquals(2, results.size());
        verify(flightRepository, times(1)).findAllById(List.of(1L, 2L));
    }

    @Test
    void shouldTriggerColdStartFallbackWhenAiFails() {
        // Arrange: Make the RestClient throw an exception
        when(aiRestClient.get()).thenThrow(new RuntimeException("AI is offline"));

        Flight fallbackFlight = new Flight();
        when(flightRepository.findTop5ByOrderByRouteArrivalAirportPopularityScoreDesc())
                .thenReturn(List.of(fallbackFlight));

        // Act
        List<Flight> results = recommendationService.getRecommendations("user123");

        // Assert
        assertEquals(1, results.size());
        verify(flightRepository, times(1)).findTop5ByOrderByRouteArrivalAirportPopularityScoreDesc();
    }
}