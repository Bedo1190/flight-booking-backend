package com.spheretech.flight_booking_backend.repository;

import com.spheretech.flight_booking_backend.model.Airline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AirlineRepository extends JpaRepository<Airline, Long> {
    List<Airline> findByNameContainingIgnoreCase(String name);
}
