package com.sportygroup.f1betting.external.dto;

import lombok.Builder;

@Builder
public record DriverDto(String externalDriverId,
                        String fullName,
                        String externalEventId) {
}
