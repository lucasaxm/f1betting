package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.external.F1ExternalApi;
import com.sportygroup.f1betting.external.dto.ExternalEventDto;
import com.sportygroup.f1betting.entity.ProviderName;
import com.sportygroup.f1betting.properties.F1ApiProperties;
import com.sportygroup.f1betting.repository.EventExternalRefRepository;
import com.sportygroup.f1betting.repository.EventRepository;
import com.sportygroup.f1betting.repository.SyncStatusRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.OffsetDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"})
class SyncServiceTest {
    @Autowired
    EventRepository eventRepository;
    @Autowired
    EventExternalRefRepository eventExternalRefRepository;
    @Autowired
    SyncStatusRepository syncStatusRepository;

    @Test
    void upsertEventDeduplicatesByNaturalKey() {
        SyncService service = getSyncService();

        ExternalEventDto first = ExternalEventDto.builder()
            .externalEventId("A")
            .providerName(ProviderName.OPENF1)
            .eventName("Race")
            .eventType("Race")
            .year(2024)
            .countryName("Belgium")
            .dateStart(OffsetDateTime.parse("2024-08-25T14:00:00Z"))
            .build();
        service.upsertEvent(first);

        ExternalEventDto second = ExternalEventDto.builder()
            .externalEventId("B")
            .providerName(ProviderName.ERGAST)
            .eventName("Race")
            .eventType("Race")
            .year(2024)
            .countryName("Belgium")
            .dateStart(OffsetDateTime.parse("2024-08-25T14:00:00Z"))
            .build();
        service.upsertEvent(second);

        assertEquals(1, eventRepository.count());
        assertEquals(2, eventExternalRefRepository.count());
    }

    private SyncService getSyncService() {
        F1ExternalApi dummy = new F1ExternalApi() {
            @Override
            public java.util.List<ExternalEventDto> listEvents(Integer year, String type, String country) {
                return Collections.emptyList();
            }

            @Override
            public java.util.List<com.sportygroup.f1betting.external.dto.DriverDto> listDrivers(
                String externalEventId) {
                return Collections.emptyList();
            }
        };
        F1ApiProperties props = new F1ApiProperties();
        props.setActiveProvider("openf1");
        return new SyncService(syncStatusRepository, eventRepository, eventExternalRefRepository, dummy, props);
    }
}
