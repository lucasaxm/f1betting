package com.sportygroup.f1betting.external.dto.openf1;

import com.sportygroup.f1betting.external.dto.EventDto;

import java.time.OffsetDateTime;

public record OpenF1SessionDto(String sessionKey,
                               long meetingKey,
                               String sessionName,
                               String sessionType,
                               int year,
                               String countryName,
                               String dateStart,
                               String dateEnd) {

    public EventDto toEventSearchDto() {
        return EventDto.builder()
            .externalEventId(this.sessionKey)
            .providerName("openf1")
            .eventName(this.sessionName)
            .eventType(this.sessionType)
            .year(this.year)
            .countryName(this.countryName)
            .startDate(OffsetDateTime.parse(dateStart))
            .build();
    }
}
