package com.spheretech.flight_booking_backend.service;

import com.spheretech.flight_booking_backend.dto.AirportRequest;
import com.spheretech.flight_booking_backend.model.Airport;
import com.spheretech.flight_booking_backend.repository.AirportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AirportService {

    private final AirportRepository airportRepository;

    public Airport addAirport(AirportRequest request) {
        Airport airport = new Airport();
        airport.setName(request.name());
        airport.setCity(request.city());
        airport.setCode(request.code());
        airport.setCountry(request.country());
        airport.setRegion(request.region());
        airport.setLatitude(request.latitude());
        airport.setLongitude(request.longitude());
        return airportRepository.save(airport);
    }

    public List<Airport> searchAirports(String name) {
        return airportRepository.findByNameContainingIgnoreCase(name);
    }
}