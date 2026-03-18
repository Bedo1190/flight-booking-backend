package com.spheretech.flight_booking_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spheretech.flight_booking_backend.model.Flight;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
    List<Flight> findByAirlineId(Long airlineId);
    // Cold Start query: Gets flights ordered by the destination airport's popularity
    List<Flight> findTop5ByOrderByRouteArrivalAirportPopularityScoreDesc(); 
}
