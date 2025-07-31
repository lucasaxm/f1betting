package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.entity.DriverExternalRef;
import com.sportygroup.f1betting.entity.Event;
import com.sportygroup.f1betting.entity.EventExternalRef;
import com.sportygroup.f1betting.entity.EventOdd;
import com.sportygroup.f1betting.repository.EventOddRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class OddsSynchronizer {
    private final EventOddRepository eventOddRepository;

    public void syncOdds(Map<EventExternalRef, List<DriverExternalRef>> driversByEvent) {
        driversByEvent.entrySet().stream()
            .filter(entry -> !eventHasDrivers(entry) || eventIsClosed(entry))
            .forEach(entry -> {
                Event event = entry.getKey().getEvent();
                List<DriverExternalRef> drivers = entry.getValue();
                drivers.forEach(driverRef -> {
                    EventOdd odd = new EventOdd();
                    odd.setEvent(event);
                    odd.setDriver(driverRef.getDriver());
                    odd.setOdd((short) (2 + ThreadLocalRandom.current().nextInt(3)));
                    odd.setCreatedAt(OffsetDateTime.now());
                    log.info("Creating event odd for event {} and driver {} with odd {}",
                        event.getId(), driverRef.getExternalId(), odd.getOdd());
                    eventOddRepository.save(odd);
                });
            });
    }

    private boolean eventHasDrivers(Map.Entry<EventExternalRef, List<DriverExternalRef>> entry) {
        List<DriverExternalRef> drivers = entry.getValue();
        if (drivers == null || drivers.isEmpty()) {
            log.warn("No drivers found for event {}", entry.getKey().getExternalId());
            return false;
        }
        return true;
    }

    private boolean eventIsClosed(Map.Entry<EventExternalRef, List<DriverExternalRef>> entry) {
        Event event = entry.getKey().getEvent();
        if (event.getWinnerDriverId() != null) {
            log.info("Event {} already has a winner driver set, skipping odds synchronization", event.getId());
            return false;
        }
        return true;
    }
}
