package com.sportygroup.f1betting.controller.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record EventOutcomeResponseDto(
        UUID eventId,
        UUID winnerDriverId,
        int betsSettled,
        BigDecimal totalPaid
) {}
