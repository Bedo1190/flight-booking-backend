package com.spheretech.flight_booking_backend.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FlightTest {

    @Test
    void shouldReturnBasePriceWhenEmpty() {
        Flight flight = new Flight();
        flight.setBasePrice(100.0);
        flight.setTotalCapacity(100);
        flight.setOccupiedSeats(0); // 0% full

        assertEquals(100.0, flight.getCurrentPrice());
    }

    @Test
    void shouldIncreasePriceByTenPercentWhenTenPercentFull() {
        Flight flight = new Flight();
        flight.setBasePrice(100.0);
        flight.setTotalCapacity(100);
        flight.setOccupiedSeats(10); // 10% full

        // 100 * 1.10^1 = 110.0
        assertEquals(110.00, flight.getCurrentPrice(), 0.01);
    }

    @Test
    void shouldIncreasePriceByTwentyOnePercentWhenTwentyPercentFull() {
        Flight flight = new Flight();
        flight.setBasePrice(100.0);
        flight.setTotalCapacity(100);
        flight.setOccupiedSeats(25); // 25% full, meaning two 10% increments

        // 100 * 1.10^2 = 121.0
        assertEquals(121.00, flight.getCurrentPrice(), 0.01);
    }
    
    @Test
    void shouldReturnBasePriceWhenCapacityIsZero() {
        Flight flight = new Flight();
        flight.setBasePrice(100.0);
        flight.setTotalCapacity(0); // Prevent division by zero
        flight.setOccupiedSeats(0);

        assertEquals(100.0, flight.getCurrentPrice());
    }
}