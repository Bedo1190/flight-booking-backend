package com.spheretech.flight_booking_backend.util;

public class CardUtils {
    public static String maskCardNumber(String rawNumber) {
        if (rawNumber == null) return null;
        
        // Strip all non-numeric characters (handles -, spaces, commas)
        String clean = rawNumber.replaceAll("[^0-9]", ""); 
        
        if (clean.length() < 10) return clean; // Fallback for invalid numbers
        
        // Format: 422116******0005
        return clean.substring(0, 6) + "******" + clean.substring(clean.length() - 4); 
    }
}
