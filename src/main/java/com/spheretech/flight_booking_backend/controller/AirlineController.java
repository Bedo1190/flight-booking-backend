package com.spheretech.flight_booking_backend.controller;

import com.spheretech.flight_booking_backend.dto.AirlineRequest;
import com.spheretech.flight_booking_backend.model.Airline;
import com.spheretech.flight_booking_backend.service.AirlineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/airlines")
@RequiredArgsConstructor
public class AirlineController {

    private final AirlineService airlineService;

    @PostMapping
    public ResponseEntity<Airline> addAirline(@Valid @RequestBody AirlineRequest request) {
        return new ResponseEntity<>(airlineService.addAirline(request), HttpStatus.CREATED);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Airline>> searchAirlines(@RequestParam String name) {
        return ResponseEntity.ok(airlineService.searchAirlines(name));
    }
}