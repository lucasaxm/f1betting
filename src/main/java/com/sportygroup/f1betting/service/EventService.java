package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.controller.dto.response.EventOddDto;
import com.sportygroup.f1betting.controller.dto.response.EventResponseDto;
import com.sportygroup.f1betting.entity.Event;
import com.sportygroup.f1betting.entity.EventType;
import com.sportygroup.f1betting.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final SyncService syncService;

    @Transactional(readOnly = true)
    public Page<EventResponseDto> getEventsPage(Integer year,
                                                EventType type,
                                                String country,
                                                Pageable pageable) {

        int syncYear = year != null ? year : Year.now().getValue();
        try {
            syncService.syncYear(syncYear);
        } catch (RuntimeException ex) {
            log.error("Failed to sync year {}", syncYear, ex);
            if (syncYear == Year.now().getValue()) {
                throw ex;
            }
        }

        String countryLc = country != null ? country.toLowerCase() : null;

        Page<UUID> idPage =
            eventRepository.findIdsByFilter(year, type, countryLc, pageable);

        if (idPage.isEmpty()) {
            return idPage.map(x -> null);
        }

        List<Event> hydrated =
            eventRepository.findAllWithCurrentOddsByIdIn(idPage.getContent());

        // keep original ordering
        Map<UUID, Event> byId = hydrated.stream()
            .collect(Collectors.toMap(Event::getId, Function.identity()));

        List<EventResponseDto> dtoPage =
            idPage.getContent().stream()
                .map(byId::get)
                .map(ev -> new EventResponseDto(
                    ev,
                    ev.getEventOdds()
                        .stream()
                        .map(EventOddDto::new)
                        .toList()))
                .toList();

        return new PageImpl<>(dtoPage, pageable, idPage.getTotalElements());
    }
}
