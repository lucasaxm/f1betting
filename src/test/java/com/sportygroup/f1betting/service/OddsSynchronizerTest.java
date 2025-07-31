package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.entity.Driver;
import com.sportygroup.f1betting.entity.DriverExternalRef;
import com.sportygroup.f1betting.entity.Event;
import com.sportygroup.f1betting.entity.EventExternalRef;
import com.sportygroup.f1betting.repository.EventOddRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OddsSynchronizerTest {

    @Mock
    EventOddRepository eventOddRepository;

    @InjectMocks
    OddsSynchronizer synchronizer;

    EventExternalRef eventRef;
    DriverExternalRef driverRef;

    @BeforeEach
    void setup() {
        Event event = new Event();
        event.setId(UUID.randomUUID());
        eventRef = new EventExternalRef();
        eventRef.setId(UUID.randomUUID());
        eventRef.setExternalId("e1");
        eventRef.setEvent(event);

        Driver driver = new Driver();
        driver.setId(UUID.randomUUID());
        driver.setFullName("Driver");
        driverRef = new DriverExternalRef();
        driverRef.setDriver(driver);
        driverRef.setExternalId("1");
    }

    @Test
    void createsOddsForKnownDriver() {
        synchronizer.syncOdds(Map.of(eventRef, List.of(driverRef)));
        verify(eventOddRepository).save(any());
    }

    @Test
    void skipWhenEventIsClosed() {
        eventRef.getEvent().setWinnerDriverId(UUID.randomUUID());
        synchronizer.syncOdds(Map.of(eventRef, List.of(driverRef)));
        verify(eventOddRepository, never()).save(any());
    }

    @Test
    void skipsWhenNoDrivers() {
        synchronizer.syncOdds(Map.of(eventRef, List.of()));
        verify(eventOddRepository, never()).save(any());
    }
}
