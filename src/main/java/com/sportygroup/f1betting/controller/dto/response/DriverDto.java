package com.sportygroup.f1betting.controller.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record DriverDto(
    UUID id,
    String fullName
) {
}
