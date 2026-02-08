package com.dk.supportsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sla_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class SlaConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false, unique = true)
    private Ticket ticket;
    
    private LocalDateTime firstResponseDeadline;
    
    private LocalDateTime resolutionDeadline;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean firstResponseMet = false;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean resolutionMet = false;
    
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
