package com.spheretech.flight_booking_backend.service;

import com.spheretech.flight_booking_backend.model.Flight;
import com.spheretech.flight_booking_backend.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiRecommendationService {

    private final RestClient aiRestClient;
    private final FlightRepository flightRepository;

    public List<Flight> getRecommendations(String passengerId) {
        try {
            // 1. Call the Python Microservice
            List<Long> recommendedFlightIds = aiRestClient.get()
                    .uri("/recommend/{passengerId}", passengerId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Long>>() {});

            // 2. If the GNN found a match (Returning User)
            if (recommendedFlightIds != null && !recommendedFlightIds.isEmpty()) {
                return flightRepository.findAllById(recommendedFlightIds);
            }
            
        } catch (Exception e) {
            System.err.println("⚠️ AI Service unreachable or user has no history. Triggering Cold Start fallback...");
        }

        // 3. COLD START FALLBACK: User has no history or Python is down
        // We return the top 5 trending flights based on Airport popularity!
        return flightRepository.findTop5ByOrderByRouteArrivalAirportPopularityScoreDesc();
    }
}