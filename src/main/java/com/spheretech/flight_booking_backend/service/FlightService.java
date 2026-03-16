package com.spheretech.flight_booking_backend.service;

import com.spheretech.flight_booking_backend.dto.FlightRequest;
import com.spheretech.flight_booking_backend.exception.ResourceNotFoundException;
import com.spheretech.flight_booking_backend.model.Airline;
import com.spheretech.flight_booking_backend.model.Flight;
import com.spheretech.flight_booking_backend.model.Route;
import com.spheretech.flight_booking_backend.repository.AirlineRepository;
import com.spheretech.flight_booking_backend.repository.FlightRepository;
import com.spheretech.flight_booking_backend.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository flightRepository;
    private final AirlineRepository airlineRepository;
    private final RouteRepository routeRepository;

    public Flight addFlight(FlightRequest request) {
        Airline airline = airlineRepository.findById(request.airlineId())
                .orElseThrow(() -> new ResourceNotFoundException("Airline not found with ID: " + request.airlineId()));
                
        Route route = routeRepository.findById(request.routeId())
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with ID: " + request.routeId()));

        Flight flight = new Flight();
        flight.setAirline(airline);
        flight.setRoute(route);
        flight.setBasePrice(request.basePrice());
        flight.setTotalCapacity(request.totalCapacity());
        flight.setDepartureTime(request.departureTime());
        // occupiedSeats defaults to 0 as defined in the entity
        
        return flightRepository.save(flight);
    }

    public List<Flight> searchFlightsByAirline(Long airlineId) {
        return flightRepository.findByAirlineId(airlineId);
    }
}