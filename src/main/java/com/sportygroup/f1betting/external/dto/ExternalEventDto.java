package com.sportygroup.f1betting.external.dto;

import lombok.Builder;
import com.sportygroup.f1betting.entity.ProviderName;

import java.time.OffsetDateTime;

@Builder
public record ExternalEventDto(String externalEventId,
                               ProviderName providerName,
                               String eventName,
                               String eventType,
                               Integer year,
                               String countryName,
                               OffsetDateTime dateStart) {
}
