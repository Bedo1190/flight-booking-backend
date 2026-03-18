package com.spheretech.flight_booking_backend.repository;

import com.spheretech.flight_booking_backend.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findByDepartureAirportCodeAndArrivalAirportCode(String departureCode, String arrivalCode);
}