package com.sportygroup.f1betting.external.dto;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record ExternalEventDto(String externalEventId,
                               String providerName,
                               String eventName,
                               String eventType,
                               Integer year,
                               String countryName,
                               OffsetDateTime dateStart) {
}
