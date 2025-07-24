package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.entity.Event;
import com.sportygroup.f1betting.entity.EventExternalRef;
import com.sportygroup.f1betting.entity.Provider;
import com.sportygroup.f1betting.entity.SyncStatus;
import com.sportygroup.f1betting.external.F1ExternalApi;
import com.sportygroup.f1betting.external.dto.ExternalEventDto;
import com.sportygroup.f1betting.properties.F1ApiProperties;
import com.sportygroup.f1betting.repository.EventExternalRefRepository;
import com.sportygroup.f1betting.repository.EventRepository;
import com.sportygroup.f1betting.repository.ProviderRepository;
import com.sportygroup.f1betting.repository.SyncStatusRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class SyncService {

    private final SyncStatusRepository syncStatusRepository;
    private final EventRepository eventRepository;
    private final EventExternalRefRepository eventExternalRefRepository;
    private final F1ExternalApi f1ExternalApi;
    private final ProviderRepository providerRepository;
    private final Provider provider;

    public SyncService(SyncStatusRepository syncStatusRepository,
                       EventRepository eventRepository,
                       EventExternalRefRepository eventExternalRefRepository,
                       ProviderRepository providerRepository,
                       F1ExternalApi f1ExternalApi,
                       F1ApiProperties properties) {
        this.syncStatusRepository = syncStatusRepository;
        this.eventRepository = eventRepository;
        this.eventExternalRefRepository = eventExternalRefRepository;
        this.f1ExternalApi = f1ExternalApi;
        this.providerRepository = providerRepository;
        this.provider = providerRepository.findByName(properties.getActiveProvider())
            .orElseThrow(() -> new IllegalStateException("Unknown provider: " + properties.getActiveProvider()));
    }

    @Transactional
    public void syncYear(int year) {
        Instant epoch = Instant.EPOCH;
        if (syncStatusRepository.findByProviderAndYear(provider, year).isEmpty()) {
            syncStatusRepository.saveAndFlush(new SyncStatus(null, provider, year, epoch));
        }

        SyncStatus status = syncStatusRepository.findByProviderAndYearForUpdate(provider, year)
            .orElseThrow();
        log.info("Provider {} year {} locked for sync", provider.getName(), year);

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
        log.info("Fetched {} events for provider {} year {}", events.size(), provider.getName(), year);
        for (ExternalEventDto dto : events) {
            upsertEvent(dto);
        }

        status.setLastSynced(now);
        syncStatusRepository.save(status);
        log.info("Sync for provider {} year {} finished", provider.getName(), year);
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

        Provider dtoProvider = providerRepository.findByName(dto.providerName().toJson())
            .orElseThrow();
        eventExternalRefRepository
            .findByProviderAndExternalId(dtoProvider, dto.externalEventId())
            .orElseGet(() -> {
                EventExternalRef ref = new EventExternalRef();
                ref.setProvider(dtoProvider);
                ref.setExternalId(dto.externalEventId());
                ref.setEvent(event);
                return eventExternalRefRepository.save(ref);
            });
    }
}
