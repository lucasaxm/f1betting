package com.sportygroup.f1betting.external.dto.response;

import com.sportygroup.f1betting.entity.Event;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
public record EventResponseDto(UUID id,
                               String eventName,
                               String eventType,
                               Integer year,
                               String countryName,
                               OffsetDateTime dateStart) {
    public EventResponseDto(Event event) {
        this(event.getId(),
            event.getName(),
            event.getType(),
            event.getYear(),
            event.getCountry(),
            event.getDateStart());
    }
}
