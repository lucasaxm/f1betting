package com.sportygroup.f1betting.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sportygroup.f1betting.entity.EventOdd;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
public record EventOddDto(
    UUID id,
    Short value,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    OffsetDateTime createdAt,
    DriverDto driver
) {
    public EventOddDto(EventOdd eventOdd) {
        this(eventOdd.getId(),
            eventOdd.getOdd(),
            eventOdd.getCreatedAt(),
            DriverDto.builder()
                .id(eventOdd.getDriver().getId())
                .fullName(eventOdd.getDriver().getFullName())
                .build());
    }
}
