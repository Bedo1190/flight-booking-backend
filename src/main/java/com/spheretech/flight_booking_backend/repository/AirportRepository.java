package com.spheretech.flight_booking_backend.repository;

import com.spheretech.flight_booking_backend.model.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AirportRepository extends JpaRepository<Airport, String> {
    List<Airport> findByNameContainingIgnoreCase(String name);
}