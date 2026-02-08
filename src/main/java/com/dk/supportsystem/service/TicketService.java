package com.dk.supportsystem.service;

import com.dk.supportsystem.dto.request.AddMessageRequest;
import com.dk.supportsystem.dto.request.AssignTicketRequest;
import com.dk.supportsystem.dto.request.CreateTicketRequest;
import com.dk.supportsystem.dto.request.UpdateTicketStatusRequest;
import com.dk.supportsystem.dto.response.MessageResponse;
import com.dk.supportsystem.dto.response.TicketResponse;
import com.dk.supportsystem.dto.response.UserResponse;
import com.dk.supportsystem.entity.*;
import com.dk.supportsystem.enums.TicketStatus;
import com.dk.supportsystem.enums.UserRole;
import com.dk.supportsystem.exception.BadRequestException;
import com.dk.supportsystem.exception.ResourceNotFoundException;
import com.dk.supportsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {
    
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketMessageRepository messageRepository;
    private final SlaConfigRepository slaConfigRepository;
    
    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request, Long userId) {
        User user = getUserById(userId);
        
        Ticket ticket = Ticket.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(TicketStatus.OPEN)
                .priority(request.getPriority())
                .organization(user.getOrganization())
                .createdBy(user)
                .build();
        
        ticket = ticketRepository.save(ticket);
        
        createSlaConfig(ticket);
        
        return mapToTicketResponse(ticket);
    }
    
    @Transactional(readOnly = true)
    public Page<TicketResponse> getTickets(Long userId, TicketStatus status, 
                                           Long assignedTo, Pageable pageable) {
        User user = getUserById(userId);
        Long orgId = user.getOrganization().getId();
        
        Page<Ticket> tickets = ticketRepository.findByFilters(orgId, status, assignedTo, pageable);
        return tickets.map(this::mapToTicketResponse);
    }
    
    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long ticketId, Long userId) {
        Ticket ticket = getTicketAndValidateAccess(ticketId, userId);
        return mapToTicketResponse(ticket);
    }
    
    @Transactional
    public TicketResponse assignTicket(Long ticketId, AssignTicketRequest request, Long userId) {
        Ticket ticket = getTicketAndValidateAccess(ticketId, userId);
        User agent = getUserById(request.getAgentId());
        
        if (agent.getRole() != UserRole.AGENT) {
            throw new BadRequestException("Can only assign to agents");
        }
        
        if (!agent.getOrganization().getId().equals(ticket.getOrganization().getId())) {
            throw new BadRequestException("Agent must be from same organization");
        }
        
        ticket.setAssignedTo(agent);
        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }
        
        ticket = ticketRepository.save(ticket);
        return mapToTicketResponse(ticket);
    }
    
    @Transactional
    public TicketResponse updateStatus(Long ticketId, UpdateTicketStatusRequest request, Long userId) {
        Ticket ticket = getTicketAndValidateAccess(ticketId, userId);
        
        validateStatusTransition(ticket.getStatus(), request.getStatus());
        
        ticket.setStatus(request.getStatus());
        
        if (request.getStatus() == TicketStatus.RESOLVED) {
            ticket.setResolvedAt(LocalDateTime.now());
        } else if (request.getStatus() == TicketStatus.CLOSED) {
            ticket.setClosedAt(LocalDateTime.now());
        }
        
        ticket = ticketRepository.save(ticket);
        return mapToTicketResponse(ticket);
    }
    
    @Transactional
    public MessageResponse addMessage(Long ticketId, AddMessageRequest request, Long userId) {
        Ticket ticket = getTicketAndValidateAccess(ticketId, userId);
        User user = getUserById(userId);
        
        TicketMessage message = TicketMessage.builder()
                .ticket(ticket)
                .user(user)
                .message(request.getMessage())
                .isInternal(request.getIsInternal())
                .build();
        
        message = messageRepository.save(message);
        
        updateSlaOnFirstResponse(ticket);
        
        return mapToMessageResponse(message);
    }
    
    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages(Long ticketId, Long userId) {
        getTicketAndValidateAccess(ticketId, userId);
        List<TicketMessage> messages = messageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        return messages.stream().map(this::mapToMessageResponse).toList();
    }
    
    private Ticket getTicketAndValidateAccess(Long ticketId, Long userId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        
        User user = getUserById(userId);
        if (!ticket.getOrganization().getId().equals(user.getOrganization().getId())) {
            throw new BadRequestException("Access denied");
        }
        
        return ticket;
    }
    
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    
    private void validateStatusTransition(TicketStatus current, TicketStatus next) {
        if (current == TicketStatus.CLOSED) {
            throw new BadRequestException("Cannot change status of closed ticket");
        }
    }
    
    private void createSlaConfig(Ticket ticket) {
        int hoursToAdd = switch (ticket.getPriority()) {
            case URGENT -> 1;
            case HIGH -> 4;
            case MEDIUM -> 8;
            case LOW -> 24;
        };
        
        SlaConfig slaConfig = SlaConfig.builder()
                .ticket(ticket)
                .firstResponseDeadline(LocalDateTime.now().plusHours(hoursToAdd))
                .resolutionDeadline(LocalDateTime.now().plusHours(hoursToAdd * 4))
                .build();
        
        slaConfigRepository.save(slaConfig);
    }
    
    private void updateSlaOnFirstResponse(Ticket ticket) {
        slaConfigRepository.findByTicketId(ticket.getId()).ifPresent(sla -> {
            if (!sla.getFirstResponseMet()) {
                sla.setFirstResponseMet(true);
                slaConfigRepository.save(sla);
            }
        });
    }
    
    private TicketResponse mapToTicketResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .createdBy(mapToUserResponse(ticket.getCreatedBy()))
                .assignedTo(ticket.getAssignedTo() != null ? mapToUserResponse(ticket.getAssignedTo()) : null)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .resolvedAt(ticket.getResolvedAt())
                .closedAt(ticket.getClosedAt())
                .build();
    }
    
    private MessageResponse mapToMessageResponse(TicketMessage message) {
        return MessageResponse.builder()
                .id(message.getId())
                .message(message.getMessage())
                .isInternal(message.getIsInternal())
                .user(mapToUserResponse(message.getUser()))
                .createdAt(message.getCreatedAt())
                .build();
    }
    
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .organizationName(user.getOrganization().getName())
                .build();
    }
}
