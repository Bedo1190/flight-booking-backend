package com.spheretech.flight_booking_backend.controller;

import com.spheretech.flight_booking_backend.model.Flight;
import com.spheretech.flight_booking_backend.service.AiRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final AiRecommendationService recommendationService;

    @GetMapping("/{passengerId}")
    public ResponseEntity<List<Flight>> getRecommendations(@PathVariable String passengerId) {
        return ResponseEntity.ok(recommendationService.getRecommendations(passengerId));
    }
}