package com.sportygroup.f1betting.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sportygroup.f1betting.entity.Bet;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.math.BigDecimal;

@Builder
public record BetResponseDto(
    UUID betId,
    String status,
    UUID eventId,
    UUID driverId,
    Short odd,
    BigDecimal amount,
    BigDecimal remainingBalance,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    OffsetDateTime createdAt
) {
    public BetResponseDto(Bet bet, BigDecimal remainingBalance) {
        this(bet.getId(),
            bet.getStatus(),
            bet.getEvent().getId(),
            bet.getEventOdds().getDriver().getId(),
            bet.getEventOdds().getOdd(),
            bet.getAmount(),
            remainingBalance,
            bet.getCreatedAt());
    }
}
