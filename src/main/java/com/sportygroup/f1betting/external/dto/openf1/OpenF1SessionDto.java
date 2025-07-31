package com.sportygroup.f1betting.external.dto.openf1;

import com.sportygroup.f1betting.entity.EventType;
import com.sportygroup.f1betting.entity.ProviderName;
import com.sportygroup.f1betting.external.dto.ExternalEventDto;

import java.time.OffsetDateTime;
import java.util.Optional;

public record OpenF1SessionDto(String sessionKey,
                               long meetingKey,
                               String sessionName,
                               String sessionType,
                               int year,
                               String countryName,
                               String dateStart,
                               String dateEnd) {

    public ExternalEventDto toEventSearchDto() {
        return ExternalEventDto.builder()
            .externalEventId(this.sessionKey)
            .providerName(ProviderName.OPENF1)
            .eventName(this.sessionName)
            .eventType(Optional.ofNullable(this.sessionType)
                .map(String::toUpperCase)
                .map(EventType::valueOf)
                .orElse(null))
            .year(this.year)
            .countryName(this.countryName)
            .dateStart(OffsetDateTime.parse(dateStart))
            .build();
    }
}
