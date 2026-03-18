package com.spheretech.flight_booking_backend.controller;

import com.spheretech.flight_booking_backend.dto.RouteRequest;
import com.spheretech.flight_booking_backend.model.Route;
import com.spheretech.flight_booking_backend.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @PostMapping
    public ResponseEntity<Route> addRoute(@Valid @RequestBody RouteRequest request) {
        return new ResponseEntity<>(routeService.addRoute(request), HttpStatus.CREATED);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Route>> searchRoutes(
            @RequestParam String departureCode, 
            @RequestParam String arrivalCode) {
        return ResponseEntity.ok(routeService.searchRoutes(departureCode, arrivalCode));
    }
}