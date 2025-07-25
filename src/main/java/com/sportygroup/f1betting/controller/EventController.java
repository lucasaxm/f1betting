package com.sportygroup.f1betting.controller;

import com.sportygroup.f1betting.external.dto.response.EventResponseDto;
import com.sportygroup.f1betting.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/events")
@RequiredArgsConstructor
public class EventController {

    private static final Map<String, String> SORT_MAPPING = Map.of(
        "year", "year",
        "name", "name",
        "date_start", "dateStart",
        "country", "country",
        "type", "type"
    );
    private final EventService eventService;

    @GetMapping
    public PageResponse<EventResponseDto> getEvents(Integer year,
                                                    String type,
                                                    String country,
                                                    @PageableDefault(size = 20)
                                                    @SortDefault(sort = "year", direction = Sort.Direction.DESC)
                                                    @SortDefault(sort = "date_start", direction = Sort.Direction.DESC)
                                                    @SortDefault(sort = "name", direction = Sort.Direction.ASC)
                                                    Pageable pageable) {
        Pageable translated = translate(pageable);
        Page<EventResponseDto> page = eventService.getEventsPage(year, type, country, translated);
        return PageResponse.from(page);
    }

    private Pageable translate(Pageable pageable) {
        List<Sort.Order> orders = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            String mapped = SORT_MAPPING.get(order.getProperty());
            if (mapped == null) {
                throw new IllegalArgumentException("Unknown sort field '" + order.getProperty() + "'");
            }
            orders.add(new Sort.Order(order.getDirection(), mapped));
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));
    }

}
