package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.entity.Event;
import com.sportygroup.f1betting.entity.EventExternalRef;
import com.sportygroup.f1betting.external.dto.EventDto;
import com.sportygroup.f1betting.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Year;

@Service
@RequiredArgsConstructor
public class EventService {
    private final SyncService syncService;
    private final EventRepository eventRepository;

    public Page<EventDto> getEventsPage(Integer year, String type, String country, Pageable pageable) {
        int syncYear = year != null ? year : Year.now().getValue();
        try {
            syncService.syncYear(syncYear);
        } catch (RuntimeException ex) {
            if (syncYear == Year.now().getValue()) {
                throw ex;
            }
        }

        Page<Event> page = eventRepository.findByFilter(year, type, country, pageable);
        return page.map(event -> {
            EventExternalRef ref = event.getEventExternalRefs().stream().findFirst().orElse(null);
            return EventDto.builder()
                .externalEventId(ref != null ? ref.getExternalId() : null)
                .providerName(ref != null ? ref.getProviderName() : null)
                .eventName(event.getName())
                .eventType(event.getType())
                .year(event.getYear())
                .countryName(event.getCountry())
                .dateStart(event.getDateStart())
                .build();
        });
    }
}
