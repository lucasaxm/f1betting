package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.entity.Event;
import com.sportygroup.f1betting.external.dto.response.EventResponseDto;
import com.sportygroup.f1betting.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Year;

@Service
@RequiredArgsConstructor
public class EventService {
    private final SyncService syncService;
    private final EventRepository eventRepository;

    public Page<EventResponseDto> getEventsPage(Integer year, String type, String country, Pageable pageable) {
        int syncYear = year != null ? year : Year.now().getValue();
        try {
            syncService.syncYear(syncYear);
        } catch (RuntimeException ex) {
            if (syncYear == Year.now().getValue()) {
                throw ex;
            }
        }

        Page<Event> page = eventRepository.findByFilter(year, type, country, pageable);
        return page.map(EventResponseDto::new);
    }
}
