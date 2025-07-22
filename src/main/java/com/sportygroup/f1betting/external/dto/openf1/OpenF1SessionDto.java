package com.sportygroup.f1betting.external.dto.openf1;

import com.sportygroup.f1betting.external.dto.ExternalEventDto;
import com.sportygroup.f1betting.entity.ProviderName;

import java.time.OffsetDateTime;

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
            .eventType(this.sessionType)
            .year(this.year)
            .countryName(this.countryName)
            .dateStart(OffsetDateTime.parse(dateStart))
            .build();
    }
}
