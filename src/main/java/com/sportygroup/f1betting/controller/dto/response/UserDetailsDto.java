package com.sportygroup.f1betting.controller.dto.response;

import com.sportygroup.f1betting.entity.User;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Detailed representation of a user including their bets.
 */
@Builder
public record UserDetailsDto(
        UUID id,
        String name,
        BigDecimal balance,
        List<UserBetDto> bets
) {
    public UserDetailsDto(User user, List<UserBetDto> bets) {
        this(user.getId(), user.getName(), user.getBalance(), bets);
    }
}
