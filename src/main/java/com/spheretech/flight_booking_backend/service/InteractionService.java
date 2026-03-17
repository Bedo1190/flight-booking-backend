package com.spheretech.flight_booking_backend.service;

import com.spheretech.flight_booking_backend.model.UserInteraction;
import com.spheretech.flight_booking_backend.repository.UserInteractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InteractionService {

    private final UserInteractionRepository interactionRepository;

    public void logInteraction(String passengerId, Long flightId, String type) {
        UserInteraction interaction = new UserInteraction();
        interaction.setPassengerId(passengerId);
        interaction.setFlightId(flightId);
        interaction.setInteractionType(type);
        interactionRepository.save(interaction);
    }
}