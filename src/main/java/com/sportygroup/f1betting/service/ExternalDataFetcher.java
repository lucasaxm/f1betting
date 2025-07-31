package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.entity.EventExternalRef;
import com.sportygroup.f1betting.external.F1ExternalApi;
import com.sportygroup.f1betting.external.dto.ExternalDriverDto;
import com.sportygroup.f1betting.external.dto.ExternalEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalDataFetcher {

    private final F1ExternalApi f1ExternalApi;

    public List<ExternalEventDto> fetchEvents(int year) {
        List<ExternalEventDto> events = f1ExternalApi.listEvents(year, null, null);
        log.info("Fetched {} external events for year {}", events.size(), year);
        return events;
    }

    public Map<EventExternalRef, List<ExternalDriverDto>> fetchDrivers(List<EventExternalRef> events) {
        return events.stream()
            .collect(Collectors.toMap(Function.identity(), event -> {
                List<ExternalDriverDto> drivers = f1ExternalApi.listDrivers(event.getExternalId());
                log.info("Fetched {} drivers for event {}", drivers.size(), event.getExternalId());
                return drivers;
            }));
    }
}
