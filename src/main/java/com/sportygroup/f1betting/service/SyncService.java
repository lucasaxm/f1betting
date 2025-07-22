package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.entity.Event;
import com.sportygroup.f1betting.entity.EventExternalRef;
import com.sportygroup.f1betting.entity.SyncStatus;
import com.sportygroup.f1betting.external.F1ExternalApi;
import com.sportygroup.f1betting.external.dto.ExternalEventDto;
import com.sportygroup.f1betting.repository.EventExternalRefRepository;
import com.sportygroup.f1betting.repository.EventRepository;
import com.sportygroup.f1betting.repository.SyncStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
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
        log.info("Year {} locked for sync", year);

        int currentYear = Year.now().getValue();
        Instant now = Instant.now();

        if (year < currentYear && !status.getLastSynced().equals(epoch)) {
            log.info("Skip year {} - already synced at {}", year, status.getLastSynced());
            return;
        }
        if (year == currentYear &&
            status.getLastSynced().plus(1, ChronoUnit.HOURS).isAfter(now)) {
            log.info("Skip year {} - synced recently at {}", year, status.getLastSynced());
            return;
        }

        List<ExternalEventDto> events = f1ExternalApi.listEvents(year, null, null);
        log.info("Fetched {} events for year {}", events.size(), year);
        for (ExternalEventDto dto : events) {
            upsertEvent(dto);
        }

        status.setLastSynced(now);
        syncStatusRepository.save(status);
        log.info("Sync for year {} finished", year);
    }

    void upsertEvent(ExternalEventDto dto) {
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
