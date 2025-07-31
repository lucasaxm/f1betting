package com.sportygroup.f1betting.external.dto;

import com.sportygroup.f1betting.entity.EventType;
import com.sportygroup.f1betting.entity.ProviderName;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record ExternalEventDto(String externalEventId,
                               ProviderName providerName,
                               String eventName,
                               EventType eventType,
                               Integer year,
                               String countryName,
                               OffsetDateTime dateStart) {
}
