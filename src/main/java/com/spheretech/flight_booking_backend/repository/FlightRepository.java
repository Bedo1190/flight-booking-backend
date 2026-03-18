package com.spheretech.flight_booking_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spheretech.flight_booking_backend.model.Flight;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
    
    List<Flight> findByAirlineId(Long airlineId);
    
    // Cold Start query: Gets flights ordered by the destination airport's popularity
    List<Flight> findTop5ByOrderByRouteArrivalAirportPopularityScoreDesc(); 

    //Master search query
    @Query("SELECT f FROM Flight f WHERE " +
           "f.route.departureAirport.code = :departureCode AND " +
           "f.route.arrivalAirport.code = :arrivalCode AND " +
           "f.departureTime >= :startBoundary AND f.departureTime < :endBoundary")
    List<Flight> searchFlightsByRouteAndDateRange(
            @Param("departureCode") String departureCode, 
            @Param("arrivalCode") String arrivalCode, 
            @Param("startBoundary") java.time.LocalDateTime startBoundary,
            @Param("endBoundary") java.time.LocalDateTime endBoundary);
}