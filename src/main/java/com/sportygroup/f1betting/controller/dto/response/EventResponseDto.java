package com.sportygroup.f1betting.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sportygroup.f1betting.entity.Event;
import com.sportygroup.f1betting.entity.EventType;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Builder
public record EventResponseDto(UUID id,
                               String eventName,
                               EventType eventType,
                               Integer year,
                               String countryName,
                               @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
                               OffsetDateTime dateStart,
                               DriverDto winner,
                               List<EventOddDto> odds) {
    public EventResponseDto(Event event, List<EventOddDto> odds) {
        this(event.getId(),
            event.getName(),
            event.getType(),
            event.getYear(),
            event.getCountry(),
            event.getDateStart(),
            getWinnerDriver(event, odds),
            odds);
    }

    private static DriverDto getWinnerDriver(Event event, List<EventOddDto> odds) {
        return Optional.ofNullable(event.getWinnerDriverId())
            .map(driverId -> odds.stream().filter(odd -> odd.driver().id().equals(driverId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No odds found for the event")))
            .map(EventOddDto::driver)
            .orElse(null);
    }
}
