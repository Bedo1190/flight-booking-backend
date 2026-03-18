package com.spheretech.flight_booking_backend.service;

import com.spheretech.flight_booking_backend.model.UserInteraction;
import com.spheretech.flight_booking_backend.repository.UserInteractionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InteractionServiceTest {

    @Mock
    private UserInteractionRepository interactionRepository;

    @InjectMocks
    private InteractionService interactionService;

    @Test
    void shouldLogInteractionSuccessfully() {
        // Act
        interactionService.logInteraction("test@example.com", 1L, "SEARCH");

        // Assert
        verify(interactionRepository, times(1)).save(any(UserInteraction.class));
    }
}