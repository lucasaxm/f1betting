package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.controller.dto.response.EventResponseDto;
import com.sportygroup.f1betting.entity.Driver;
import com.sportygroup.f1betting.entity.Event;
import com.sportygroup.f1betting.entity.EventOdd;
import com.sportygroup.f1betting.entity.EventType;
import com.sportygroup.f1betting.repository.CurrentEventOddsViewRepository;
import com.sportygroup.f1betting.repository.EventOddRepository;
import com.sportygroup.f1betting.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    SyncService syncService;
    @Mock
    EventRepository eventRepository;
    @Mock
    CurrentEventOddsViewRepository oddsViewRepository;
    @Mock
    EventOddRepository eventOddRepository;

    @InjectMocks
    EventService eventService;

    Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void pastYearSyncFailureIsIgnored() {
        int year = 2000; // definitely not current year
        Event event = buildEvent(year);
        Page<UUID> page = new PageImpl<>(List.of(event.getId()), pageable, 1);
        when(eventRepository.findIdsByFilter(year, null, null, pageable)).thenReturn(page);

        buildOdd(event);
        when(eventRepository.findAllWithCurrentOddsByIdIn(List.of(event.getId())))
            .thenReturn(List.of(event));

        doThrow(new RuntimeException("fail")).when(syncService).syncYear(year);

        Page<EventResponseDto> result = eventService.getEventsPage(year, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        EventResponseDto dto = result.getContent().getFirst();
        assertThat(dto.year()).isEqualTo(year);
        assertThat(dto.winner().fullName()).isEqualTo("Driver");
        verify(syncService).syncYear(year);
    }

    @Test
    void currentYearSyncFailurePropagates() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        doThrow(new RuntimeException("fail")).when(syncService).syncYear(year);

        assertThrows(RuntimeException.class, () -> eventService.getEventsPage(year, null, null, pageable));
        verify(syncService).syncYear(year);
    }

    private Event buildEvent(int year) {
        Event event = new Event();
        event.setId(UUID.randomUUID());
        event.setName("Race");
        event.setYear(year);
        event.setCountry("X");
        event.setType(EventType.RACE);
        event.setDateStart(OffsetDateTime.now());
        return event;
    }

    private void buildOdd(Event event) {
        Driver driver = new Driver();
        driver.setId(UUID.randomUUID());
        driver.setFullName("Driver");

        EventOdd odd = new EventOdd();
        odd.setId(UUID.randomUUID());
        odd.setEvent(event);
        odd.setDriver(driver);
        odd.setOdd((short) 2);
        odd.setCreatedAt(OffsetDateTime.now());

        event.setWinnerDriverId(driver.getId());
        event.getEventOdds().add(odd);
    }
}
