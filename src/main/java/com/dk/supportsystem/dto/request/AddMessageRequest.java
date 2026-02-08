package com.dk.supportsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddMessageRequest {
    
    @NotBlank(message = "Message is required")
    private String message;
    
    @NotNull(message = "Internal flag is required")
    private Boolean isInternal;
}
