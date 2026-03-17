package com.spheretech.flight_booking_backend.repository;

import com.spheretech.flight_booking_backend.model.UserInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserInteractionRepository extends JpaRepository<UserInteraction, Long> {
    List<UserInteraction> findByPassengerId(String passengerId);
}