package com.sportygroup.f1betting.controller;

import com.sportygroup.f1betting.external.dto.EventDto;
import com.sportygroup.f1betting.service.EventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public Page<EventDto> getEvents(Integer year,
                                    String type,
                                    String country,
                                    Pageable pageable) {
        return eventService.getEventsPage(year, type, country, pageable);
    }

}
