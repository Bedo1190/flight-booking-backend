package com.spheretech.flight_booking_backend.config;

import com.spheretech.flight_booking_backend.model.*;
import com.spheretech.flight_booking_backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(AirlineRepository airlineRepo,
                                   AirportRepository airportRepo,
                                   RouteRepository routeRepo,
                                   FlightRepository flightRepo) {
        return args -> {
            // Only seed if the database is empty
            if (flightRepo.count() == 0) {
                Airline airline = airlineRepo.save(new Airline(null, "SphereTech Airlines"));

                // We give Berlin a popularity of 100, and NY a popularity of 20
                // Update these three lines in your initDatabase() method:

                Airport departure = airportRepo.save(new Airport("IST", "Istanbul Airport", "Istanbul", "Türkiye", "Europe", 41.2590, 28.7404, 0));
                Airport arrival1 = airportRepo.save(new Airport("BER", "Berlin Brandenburg", "Berlin", "Germany", "Europe", 52.3667, 13.5033, 100));
                Airport arrival2 = airportRepo.save(new Airport("JFK", "JFK International", "New York", "USA", "North America", 40.6413, -73.7781, 20));

                Route route1 = routeRepo.save(new Route(null, departure, arrival1));
                Route route2 = routeRepo.save(new Route(null, departure, arrival2));

                // Generate exactly 50 flights to match our Python AI Dataset!
                for (int i = 1; i <= 50; i++) {
                    Route assignedRoute = (i % 2 == 0) ? route1 : route2; // Alternate routes
                    flightRepo.save(new Flight(null, airline, assignedRoute, 500.0, 200, 0, LocalDateTime.now().plusDays(i)));
                }

                System.out.println("✅ Database successfully seeded with 50 flights for AI testing!");
            }
        };
    }
}