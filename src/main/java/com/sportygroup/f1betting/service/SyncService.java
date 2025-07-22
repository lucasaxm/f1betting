package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.entity.Event;
import com.sportygroup.f1betting.entity.EventExternalRef;
import com.sportygroup.f1betting.entity.SyncStatus;
import com.sportygroup.f1betting.external.F1ExternalApi;
import com.sportygroup.f1betting.external.dto.EventDto;
import com.sportygroup.f1betting.repository.EventExternalRefRepository;
import com.sportygroup.f1betting.repository.EventRepository;
import com.sportygroup.f1betting.repository.SyncStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SyncService {

    private final SyncStatusRepository syncStatusRepository;
    private final EventRepository eventRepository;
    private final EventExternalRefRepository eventExternalRefRepository;
    private final F1ExternalApi f1ExternalApi;

    @Transactional
    public void syncYear(int year) {
        Instant epoch = Instant.EPOCH;
        if (!syncStatusRepository.existsById(year)) {
            syncStatusRepository.saveAndFlush(new SyncStatus(year, epoch));
        }

        SyncStatus status = syncStatusRepository.findByYearForUpdate(year)
            .orElseThrow();

        int currentYear = Year.now().getValue();
        Instant now = Instant.now();

        if (year < currentYear && !status.getLastSynced().equals(epoch)) {
            return;
        }
        if (year == currentYear &&
            status.getLastSynced().plus(1, ChronoUnit.HOURS).isAfter(now)) {
            return;
        }

        List<EventDto> events = f1ExternalApi.listEvents(year, null, null);
        for (EventDto dto : events) {
            upsertEvent(dto);
        }

        status.setLastSynced(now);
        syncStatusRepository.save(status);
    }

    void upsertEvent(EventDto dto) {
        Optional<Event> existing = eventRepository
            .findByCountryAndDateStart(dto.countryName(), dto.dateStart());
        Event event = existing.orElseGet(() -> {
            Event e = new Event();
            e.setName(dto.eventName());
            e.setYear(dto.year());
            e.setCountry(dto.countryName());
            e.setType(dto.eventType());
            e.setDateStart(dto.dateStart());
            return eventRepository.save(e);
        });

        eventExternalRefRepository
            .findByProviderNameAndExternalId(dto.providerName(), dto.externalEventId())
            .orElseGet(() -> {
                EventExternalRef ref = new EventExternalRef();
                ref.setProviderName(dto.providerName());
                ref.setExternalId(dto.externalEventId());
                ref.setEvent(event);
                return eventExternalRefRepository.save(ref);
            });
    }
}
