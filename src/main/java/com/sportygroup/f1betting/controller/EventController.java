package com.sportygroup.f1betting.controller;

import com.sportygroup.f1betting.controller.dto.request.EventOutcomeRequest;
import com.sportygroup.f1betting.controller.dto.response.EventOutcomeResponseDto;
import com.sportygroup.f1betting.controller.dto.response.EventResponseDto;
import com.sportygroup.f1betting.controller.dto.response.PageResponse;
import com.sportygroup.f1betting.entity.EventType;
import com.sportygroup.f1betting.service.EventOutcomeService;
import com.sportygroup.f1betting.service.EventService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private final EventOutcomeService outcomeService;

    @GetMapping
    public PageResponse<EventResponseDto> getEvents(@RequestParam(required = false) Integer year,
                                                    @RequestParam(required = false) EventType type,
                                                    @RequestParam(required = false) String country,
                                                    @ParameterObject
                                                    @PageableDefault(size = 20)
                                                    @SortDefault(sort = "year", direction = Sort.Direction.DESC)
                                                    @SortDefault(sort = "date_start", direction = Sort.Direction.DESC)
                                                    @SortDefault(sort = "name", direction = Sort.Direction.ASC)
                                                    @Parameter(
                                                        name = "sort",
                                                        description = """
                                                            Sorting criteria in the form `property(,asc|desc)`.
                                                            Valid properties: `year`, `name`, `date_start`, `country`, `type`.
                                                            Multiple sort parameters are supported, e.g.: `?sort=year,desc&sort=name,asc`
                                                            """,
                                                        array = @ArraySchema(schema = @Schema(type = "string"))
                                                    )
                                                    Pageable pageable) {
        Pageable translated = translate(pageable);
        return PageResponse.from(eventService.getEventsPage(year, type, country, translated));
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

    @PostMapping("/{eventId}/outcome")
    public EventOutcomeResponseDto declareOutcome(@PathVariable UUID eventId,
                                                  @RequestBody EventOutcomeRequest request) {
        return outcomeService.declareOutcome(eventId, request.winnerDriverId());
    }

}
