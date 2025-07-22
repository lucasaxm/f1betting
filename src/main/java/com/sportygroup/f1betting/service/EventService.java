package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.external.F1ExternalApi;
import com.sportygroup.f1betting.external.dto.EventDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EventService {
    private final F1ExternalApi f1ExternalApi;

    public EventService(F1ExternalApi f1ExternalApi) {
        this.f1ExternalApi = f1ExternalApi;
    }

    public Page<EventDto> getEventsPage(Integer year, String type, String country, Pageable pageable) {
        var events = f1ExternalApi.listEvents(year, type, country);
        int total = events.size();
        var pageContent = events.stream()
            .skip(pageable.getOffset())
            .limit(pageable.getPageSize())
            .toList();
        return new PageImpl<>(pageContent, pageable, total);
    }
}
