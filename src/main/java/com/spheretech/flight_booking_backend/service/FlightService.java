package com.spheretech.flight_booking_backend.service;

import com.spheretech.flight_booking_backend.dto.FlightRequest;
import com.spheretech.flight_booking_backend.exception.ResourceNotFoundException;
import com.spheretech.flight_booking_backend.model.Airline;
import com.spheretech.flight_booking_backend.model.Flight;
import com.spheretech.flight_booking_backend.model.Route;
import com.spheretech.flight_booking_backend.repository.AirlineRepository;
import com.spheretech.flight_booking_backend.repository.AirportRepository;
import com.spheretech.flight_booking_backend.repository.FlightRepository;
import com.spheretech.flight_booking_backend.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository flightRepository;
    private final AirlineRepository airlineRepository;
    private final RouteRepository routeRepository;
    private final AirportRepository airportRepository; 

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

    public List<Flight> searchFlights(String departureCode, String arrivalCode, LocalDate startDate, LocalDate endDate) {
        
        // 1. If the user didn't provide an end date, make it a 1-day search
        if (endDate == null) {
            endDate = startDate;
        }

        // 2. Start boundary is midnight of the start date
        LocalDateTime startBoundary = startDate.atStartOfDay();
        
        // 3. End boundary is midnight of the day AFTER the end date
        LocalDateTime endBoundary = endDate.plusDays(1).atStartOfDay();
        
        // 4. ML Feature: Boost the destination's popularity score!
        airportRepository.findById(arrivalCode).ifPresent(airport -> {
            airport.setPopularityScore(airport.getPopularityScore() + 1);
            airportRepository.save(airport);
        });
        
        return flightRepository.searchFlightsByRouteAndDateRange(departureCode, arrivalCode, startBoundary, endBoundary);
    }
}