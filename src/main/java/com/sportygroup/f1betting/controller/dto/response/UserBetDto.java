package com.sportygroup.f1betting.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sportygroup.f1betting.entity.Bet;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Simplified representation of a bet used for listing bets by user.
 */
@Builder
public record UserBetDto(
        UUID betId,
        String status,
        UUID eventId,
        UUID driverId,
        Short odd,
        BigDecimal amount,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        OffsetDateTime createdAt
) {
    public UserBetDto(Bet bet) {
        this(bet.getId(),
                bet.getStatus(),
                bet.getEvent().getId(),
                bet.getEventOdds().getDriver().getId(),
                bet.getEventOdds().getOdd(),
                bet.getAmount(),
                bet.getCreatedAt());
    }
}
