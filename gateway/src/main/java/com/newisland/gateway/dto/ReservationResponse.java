package com.newisland.gateway.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Optional;
import java.util.UUID;

@Data
@Builder
public class ReservationResponse {
    private UUID correlationId;
    private ReservationStatus status;
    private Optional<String> errorMessage;
}
