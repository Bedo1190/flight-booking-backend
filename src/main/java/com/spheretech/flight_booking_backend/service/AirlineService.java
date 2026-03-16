package com.spheretech.flight_booking_backend.service;

import com.spheretech.flight_booking_backend.dto.AirlineRequest;
import com.spheretech.flight_booking_backend.model.Airline;
import com.spheretech.flight_booking_backend.repository.AirlineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AirlineService {

    private final AirlineRepository airlineRepository;

    public Airline addAirline(AirlineRequest request) {
        Airline airline = new Airline();
        airline.setName(request.name());
        return airlineRepository.save(airline);
    }

    public List<Airline> searchAirlines(String name) {
        return airlineRepository.findByNameContainingIgnoreCase(name);
    }
}