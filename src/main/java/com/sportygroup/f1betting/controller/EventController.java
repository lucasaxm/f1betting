package com.sportygroup.f1betting.controller;

import com.sportygroup.f1betting.external.F1ExternalApi;
import com.sportygroup.f1betting.external.dto.EventDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("events")
public class EventController {

    private final F1ExternalApi f1ExternalApi;

    public EventController(F1ExternalApi f1ExternalApi) {
        this.f1ExternalApi = f1ExternalApi;
    }

    @GetMapping
    public List<EventDto> getEvents(Integer year,
                                    String type,
                                    String country) {
        return f1ExternalApi.listEvents(2025, null, null);
    }

}
