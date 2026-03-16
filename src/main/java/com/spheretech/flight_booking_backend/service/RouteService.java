package com.spheretech.flight_booking_backend.service;

import com.spheretech.flight_booking_backend.dto.RouteRequest;
import com.spheretech.flight_booking_backend.exception.ResourceNotFoundException;
import com.spheretech.flight_booking_backend.model.Airport;
import com.spheretech.flight_booking_backend.model.Route;
import com.spheretech.flight_booking_backend.repository.AirportRepository;
import com.spheretech.flight_booking_backend.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final AirportRepository airportRepository;

    public Route addRoute(RouteRequest request) {
        Airport departure = airportRepository.findById(request.departureAirportId())
                .orElseThrow(() -> new ResourceNotFoundException("Departure Airport not found with ID: " + request.departureAirportId()));
        
        Airport arrival = airportRepository.findById(request.arrivalAirportId())
                .orElseThrow(() -> new ResourceNotFoundException("Arrival Airport not found with ID: " + request.arrivalAirportId()));

        Route route = new Route();
        route.setDepartureAirport(departure);
        route.setArrivalAirport(arrival);
        
        return routeRepository.save(route);
    }

    public List<Route> searchRoutes(Long departureId, Long arrivalId) {
        return routeRepository.findByDepartureAirportIdAndArrivalAirportId(departureId, arrivalId);
    }
}