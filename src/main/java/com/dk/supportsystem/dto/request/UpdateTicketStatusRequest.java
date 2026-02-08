package com.dk.supportsystem.dto.request;

import com.dk.supportsystem.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateTicketStatusRequest {
    
    @NotNull(message = "Status is required")
    private TicketStatus status;
}
