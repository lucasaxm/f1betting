package com.sportygroup.f1betting.controller;

import com.sportygroup.f1betting.external.dto.EventDto;
import com.sportygroup.f1betting.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public Page<EventDto> getEvents(Integer year,
                                    String type,
                                    String country,
                                    @PageableDefault(size = 20)
                                    @SortDefault.SortDefaults({
                                        @SortDefault(sort = "dateStart", direction = org.springframework.data.domain.Sort.Direction.ASC),
                                        @SortDefault(sort = "name", direction = org.springframework.data.domain.Sort.Direction.ASC)
                                    }) Pageable pageable) {
        return eventService.getEventsPage(year, type, country, pageable);
    }

}
