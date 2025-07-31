package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.entity.Event;
import com.sportygroup.f1betting.entity.EventExternalRef;
import com.sportygroup.f1betting.entity.Provider;
import com.sportygroup.f1betting.external.dto.ExternalEventDto;
import com.sportygroup.f1betting.repository.EventExternalRefRepository;
import com.sportygroup.f1betting.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventSynchronizer {
    private final EventRepository eventRepository;
    private final EventExternalRefRepository eventExternalRefRepository;
    private final Provider provider;

    public List<EventExternalRef> syncEvents(List<ExternalEventDto> dtos) {
        return dtos.stream()
            .map(this::syncEvent)
            .toList();
    }

    private EventExternalRef syncEvent(ExternalEventDto dto) {
        checkProvider(dto);
        Event event = eventRepository
            .findByCountryIgnoreCaseAndDateStart(dto.countryName(), dto.dateStart())
            .orElseGet(Event::new);
        EventExternalRef ref = eventExternalRefRepository
            .findByProviderAndExternalId(provider, dto.externalEventId())
            .orElseGet(EventExternalRef::new);
        // Update event details only if the winner is not set
        if (event.getWinnerDriverId() == null) {
            log.info("Syncing event: {}", dto.externalEventId());
            event.setName(dto.eventName());
            event.setYear(dto.year());
            event.setCountry(dto.countryName());
            event.setType(dto.eventType());
            event.setDateStart(dto.dateStart());
            event = eventRepository.save(event);
            ref.setProvider(provider);
            ref.setExternalId(dto.externalEventId());
            ref.setEvent(event);
            ref = eventExternalRefRepository.save(ref);
        } else {
            log.info("Event {} already closed, skipping update", event.getId());
        }
        return ref;
    }

    private void checkProvider(ExternalEventDto dto) {
        if (!dto.providerName().equals(provider.getName())) {
            log.error("Event {} from provider {} does not match the active provider {}",
                dto.externalEventId(), dto.providerName(), provider.getName());
            throw new IllegalArgumentException("Event provider does not match the active provider");
        }
    }
}
