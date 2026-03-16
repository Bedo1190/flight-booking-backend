package com.spheretech.flight_booking_backend.controller;

import com.spheretech.flight_booking_backend.dto.AirportRequest;
import com.spheretech.flight_booking_backend.model.Airport;
import com.spheretech.flight_booking_backend.service.AirportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/airports")
@RequiredArgsConstructor
public class AirportController {

    private final AirportService airportService;

    @PostMapping
    public ResponseEntity<Airport> addAirport(@Valid @RequestBody AirportRequest request) {
        return new ResponseEntity<>(airportService.addAirport(request), HttpStatus.CREATED);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Airport>> searchAirports(@RequestParam String name) {
        return ResponseEntity.ok(airportService.searchAirports(name));
    }
}