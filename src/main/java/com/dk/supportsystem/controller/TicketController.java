package com.dk.supportsystem.controller;

import com.dk.supportsystem.dto.request.AddMessageRequest;
import com.dk.supportsystem.dto.request.AssignTicketRequest;
import com.dk.supportsystem.dto.request.CreateTicketRequest;
import com.dk.supportsystem.dto.request.UpdateTicketStatusRequest;
import com.dk.supportsystem.dto.response.MessageResponse;
import com.dk.supportsystem.dto.response.TicketResponse;
import com.dk.supportsystem.enums.TicketStatus;
import com.dk.supportsystem.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {
    
    private final TicketService ticketService;
    
    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ticketService.createTicket(request, userId));
    }
    
    @GetMapping
    public ResponseEntity<Page<TicketResponse>> getTickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) Long assignedTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(ticketService.getTickets(userId, status, assignedTo, pageable));
    }
    
    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> getTicket(
            @PathVariable Long ticketId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ticketService.getTicketById(ticketId, userId));
    }
    
    @PutMapping("/{ticketId}/assign")
    public ResponseEntity<TicketResponse> assignTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody AssignTicketRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ticketService.assignTicket(ticketId, request, userId));
    }
    
    @PutMapping("/{ticketId}/status")
    public ResponseEntity<TicketResponse> updateStatus(
            @PathVariable Long ticketId,
            @Valid @RequestBody UpdateTicketStatusRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ticketService.updateStatus(ticketId, request, userId));
    }
    
    @PostMapping("/{ticketId}/messages")
    public ResponseEntity<MessageResponse> addMessage(
            @PathVariable Long ticketId,
            @Valid @RequestBody AddMessageRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ticketService.addMessage(ticketId, request, userId));
    }
    
    @GetMapping("/{ticketId}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(
            @PathVariable Long ticketId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ticketService.getMessages(ticketId, userId));
    }
}
