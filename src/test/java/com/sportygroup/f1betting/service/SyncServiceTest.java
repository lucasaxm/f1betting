package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.entity.Provider;
import com.sportygroup.f1betting.entity.ProviderName;
import com.sportygroup.f1betting.entity.SyncStatus;
import com.sportygroup.f1betting.repository.SyncStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncServiceTest {

    @Mock
    SyncStatusRepository syncStatusRepository;

    @Mock
    Provider provider;

    @Mock
    ExternalDataFetcher dataFetcher;

    @Mock
    EventSynchronizer eventSynchronizer;

    @Mock
    DriverSynchronizer driverSynchronizer;

    @Mock
    OddsSynchronizer oddsSynchronizer;

    @InjectMocks
    SyncService syncService;

    @BeforeEach
    void setUp() {
        when(provider.getName()).thenReturn(ProviderName.OPENF1);
    }

    @Test
    void skipPastYearWhenAlreadySynced() {
        int pastYear = Year.now().getValue() - 1;
        Instant lastSynced = Instant.EPOCH.plus(1, ChronoUnit.DAYS);
        SyncStatus status = new SyncStatus(null, provider, pastYear, lastSynced);
        when(syncStatusRepository.findByProviderAndYear(provider, pastYear))
            .thenReturn(Optional.of(status));
        when(syncStatusRepository.findByProviderAndYearForUpdate(provider, pastYear))
            .thenReturn(Optional.of(status));

        syncService.syncYear(pastYear);

        // should not fetch or update for past years already synced
        verify(dataFetcher, never()).fetchEvents(anyInt());
        verify(eventSynchronizer, never()).syncEvents(any());
        verify(driverSynchronizer, never()).syncDrivers(any());
        verify(oddsSynchronizer, never()).syncOdds(any());
        verify(syncStatusRepository, never()).save(status);
    }

    @Test
    void skipCurrentYearIfSyncedRecently() {
        int currentYear = Year.now().getValue();
        Instant recent = Instant.now().minus(30, ChronoUnit.MINUTES);
        SyncStatus status = new SyncStatus(null, provider, currentYear, recent);
        when(syncStatusRepository.findByProviderAndYear(provider, currentYear))
            .thenReturn(Optional.of(status));
        when(syncStatusRepository.findByProviderAndYearForUpdate(provider, currentYear))
            .thenReturn(Optional.of(status));

        syncService.syncYear(currentYear);

        verify(dataFetcher, never()).fetchEvents(anyInt());
        verify(eventSynchronizer, never()).syncEvents(any());
        verify(driverSynchronizer, never()).syncDrivers(any());
        verify(oddsSynchronizer, never()).syncOdds(any());
        verify(syncStatusRepository, never()).save(status);
    }

    @Test
    void fullSyncPathWhenFirstSync() {
        int year = Year.now().getValue() - 2;
        SyncStatus existing = new SyncStatus(null, provider, year, Instant.EPOCH);
        when(syncStatusRepository.findByProviderAndYear(provider, year))
            .thenReturn(Optional.empty());
        when(syncStatusRepository.findByProviderAndYearForUpdate(provider, year))
            .thenReturn(Optional.of(existing));

        when(dataFetcher.fetchEvents(year)).thenReturn(Collections.emptyList());
        when(eventSynchronizer.syncEvents(any())).thenReturn(Collections.emptyList());
        when(dataFetcher.fetchDrivers(any())).thenReturn(Collections.emptyMap());
        when(driverSynchronizer.syncDrivers(any())).thenReturn(Collections.emptyMap());

        syncService.syncYear(year);

        // initial status record creation + final save
        verify(syncStatusRepository).saveAndFlush(argThat(s -> s.getYear() == year));
        verify(dataFetcher).fetchEvents(year);
        verify(eventSynchronizer).syncEvents(Collections.emptyList());
        verify(driverSynchronizer).syncDrivers(Collections.emptyMap());
        verify(oddsSynchronizer).syncOdds(Collections.emptyMap());
        verify(syncStatusRepository).save(existing);
    }
}
