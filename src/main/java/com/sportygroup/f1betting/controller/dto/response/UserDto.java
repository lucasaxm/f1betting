package com.sportygroup.f1betting.controller.dto.response;

import com.sportygroup.f1betting.entity.User;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Simplified representation of a user for listing users.
 */
@Builder
public record UserDto(
        UUID id,
        String name,
        BigDecimal balance
) {
    public UserDto(User user) {
        this(user.getId(), user.getName(), user.getBalance());
    }
}
