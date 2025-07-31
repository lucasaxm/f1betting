package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.entity.DriverExternalRef;
import com.sportygroup.f1betting.entity.EventExternalRef;
import com.sportygroup.f1betting.entity.Provider;
import com.sportygroup.f1betting.entity.SyncStatus;
import com.sportygroup.f1betting.external.dto.ExternalDriverDto;
import com.sportygroup.f1betting.external.dto.ExternalEventDto;
import com.sportygroup.f1betting.repository.SyncStatusRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Service
@Slf4j
public class SyncService {

    private final SyncStatusRepository syncStatusRepository;
    private final Provider provider;
    private final ExternalDataFetcher dataFetcher;
    private final EventSynchronizer eventSynchronizer;
    private final DriverSynchronizer driverSynchronizer;
    private final OddsSynchronizer oddsSynchronizer;

    public SyncService(
        SyncStatusRepository syncStatusRepository,
        Provider provider,
        ExternalDataFetcher dataFetcher,
        EventSynchronizer eventSynchronizer,
        DriverSynchronizer driverSynchronizer,
        OddsSynchronizer oddsSynchronizer
    ) {
        this.syncStatusRepository = syncStatusRepository;
        this.provider = provider;
        this.dataFetcher = dataFetcher;
        this.eventSynchronizer = eventSynchronizer;
        this.driverSynchronizer = driverSynchronizer;
        this.oddsSynchronizer = oddsSynchronizer;
    }

    @Transactional(propagation = REQUIRES_NEW)
    public void syncYear(int year) {
        // lock the sync status for the provider and year
        SyncStatus status = getSyncStatus(year);
        if (status == null) {
            return;
        }

        // fetch events for the given year from the provider API
        List<ExternalEventDto> eventDtos = dataFetcher.fetchEvents(year);

        // sync the external events with the local database and return a list of EventExternalRef
        List<EventExternalRef> eventRefs = eventSynchronizer.syncEvents(eventDtos);

        log.info("Synced {} events for provider {} year {}", eventRefs.size(), provider.getName(), year);

        // fetch drivers for each event from the provider API
        Map<EventExternalRef, List<ExternalDriverDto>> driversDtoByEventRef = dataFetcher.fetchDrivers(eventRefs);

        // sync the drivers with the local database
        Map<EventExternalRef, List<DriverExternalRef>> driversRefByEventRef =
            driverSynchronizer.syncDrivers(driversDtoByEventRef);
        log.info("Synced {} drivers for provider {} year {}", driversRefByEventRef.size(), provider.getName(), year);

        // create new odds for each driver in each event
        oddsSynchronizer.syncOdds(driversRefByEventRef);

        // Update the sync status to indicate the year has been synced
        status.setLastSynced(Instant.now());
        syncStatusRepository.save(status);
        log.info("Sync for provider {} year {} finished", provider.getName(), year);
    }

    private SyncStatus getSyncStatus(int year) {
        Instant epoch = Instant.EPOCH;
        // Ensure the sync status exists for the provider and year
        if (syncStatusRepository.findByProviderAndYear(provider, year).isEmpty()) {
            syncStatusRepository.saveAndFlush(new SyncStatus(null, provider, year, epoch));
        }

        // Lock the sync status for update to prevent concurrent modifications
        SyncStatus status = syncStatusRepository.findByProviderAndYearForUpdate(provider, year)
            .orElseThrow();
        log.info("Provider {} year {} locked for sync", provider.getName(), year);

        int currentYear = Year.now().getValue();

        // Skip syncing if the year is in the past and already synced
        if (year < currentYear && !status.getLastSynced().equals(epoch)) {
            log.info("Skip year {} - already synced at {}", year, status.getLastSynced());
            return null;
        }
        // Skip syncing if the year is the current year and synced recently
        if (year == currentYear &&
            status.getLastSynced().plus(1, ChronoUnit.HOURS).isAfter(Instant.now())) {
            log.info("Skip year {} - synced recently at {}", year, status.getLastSynced());
            return null;
        }
        return status;
    }

}
