package com.spheretech.flight_booking_backend.controller;

import com.spheretech.flight_booking_backend.dto.FlightRequest;
import com.spheretech.flight_booking_backend.model.Flight;
import com.spheretech.flight_booking_backend.service.FlightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    @PostMapping
    public ResponseEntity<Flight> addFlight(@Valid @RequestBody FlightRequest request) {
        return new ResponseEntity<>(flightService.addFlight(request), HttpStatus.CREATED);
    }

    // Moved the airline search to its own path
    @GetMapping("/airline")
    public ResponseEntity<List<Flight>> searchFlightsByAirline(@RequestParam Long airlineId) {
        return ResponseEntity.ok(flightService.searchFlightsByAirline(airlineId));
    }

    // The new Master Search endpoint
    @GetMapping("/search")
    public ResponseEntity<List<Flight>> searchFlights(
            @RequestParam String departureCode,
            @RequestParam String arrivalCode,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate) {
        
        return ResponseEntity.ok(flightService.searchFlights(departureCode, arrivalCode, startDate, endDate));
    }
}