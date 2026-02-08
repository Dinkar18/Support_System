package com.dk.supportsystem.repository;

import com.dk.supportsystem.entity.Ticket;
import com.dk.supportsystem.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    
    Page<Ticket> findByOrganizationId(Long organizationId, Pageable pageable);
    
    Page<Ticket> findByOrganizationIdAndStatus(Long organizationId, TicketStatus status, Pageable pageable);
    
    Page<Ticket> findByAssignedToId(Long assignedToId, Pageable pageable);
    
    @Query("SELECT t FROM Ticket t WHERE t.organization.id = :orgId " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:assignedTo IS NULL OR t.assignedTo.id = :assignedTo)")
    Page<Ticket> findByFilters(@Param("orgId") Long orgId,
                                @Param("status") TicketStatus status,
                                @Param("assignedTo") Long assignedTo,
                                Pageable pageable);
}
