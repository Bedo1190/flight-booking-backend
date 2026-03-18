package com.spheretech.flight_booking_backend.controller;

import com.spheretech.flight_booking_backend.dto.TicketPurchaseRequest;
import com.spheretech.flight_booking_backend.dto.TicketResponse;
import com.spheretech.flight_booking_backend.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/purchase")
    public ResponseEntity<TicketResponse> purchaseTicket(@Valid @RequestBody TicketPurchaseRequest request) {
        return new ResponseEntity<>(ticketService.purchaseTicket(request), HttpStatus.CREATED);
    }

    @GetMapping("/{ticketNumber}")
    public ResponseEntity<TicketResponse> getTicketByNumber(
            @PathVariable String ticketNumber, 
            @RequestParam String lastName) {
        return ResponseEntity.ok(ticketService.getSecuredTicket(ticketNumber, lastName));
    }

    @DeleteMapping("/{ticketNumber}")
    public ResponseEntity<Void> cancelTicket(
            @PathVariable String ticketNumber,
            @RequestParam String lastName) {
        ticketService.cancelSecuredTicket(ticketNumber, lastName);
        return ResponseEntity.noContent().build(); 
    }
}