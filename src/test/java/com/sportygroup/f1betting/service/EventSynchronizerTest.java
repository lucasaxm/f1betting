package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.entity.Event;
import com.sportygroup.f1betting.entity.EventExternalRef;
import com.sportygroup.f1betting.entity.EventType;
import com.sportygroup.f1betting.entity.Provider;
import com.sportygroup.f1betting.entity.ProviderName;
import com.sportygroup.f1betting.external.dto.ExternalEventDto;
import com.sportygroup.f1betting.repository.EventExternalRefRepository;
import com.sportygroup.f1betting.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventSynchronizerTest {

    @Mock
    EventRepository eventRepository;
    @Mock
    EventExternalRefRepository refRepository;
    @Mock
    Provider provider;

    @InjectMocks
    EventSynchronizer synchronizer;

    ExternalEventDto dto;

    @BeforeEach
    void setup() {
        when(provider.getName()).thenReturn(ProviderName.OPENF1);
        dto = ExternalEventDto.builder()
            .externalEventId("e1")
            .providerName(ProviderName.OPENF1)
            .eventName("Race")
            .eventType(EventType.RACE)
            .year(2024)
            .countryName("BE")
            .dateStart(OffsetDateTime.parse("2024-08-25T14:00:00Z"))
            .build();
    }

    @Test
    void upsertsEventAndReference() {
        when(eventRepository.findByCountryIgnoreCaseAndDateStart("BE", dto.dateStart())).thenReturn(Optional.empty());
        when(refRepository.findByProviderAndExternalId(provider, "e1")).thenReturn(Optional.empty());
        Event savedEvent = new Event();
        savedEvent.setId(UUID.randomUUID());
        when(eventRepository.save(any())).thenReturn(savedEvent);
        EventExternalRef savedRef = new EventExternalRef();
        savedRef.setId(UUID.randomUUID());
        when(refRepository.save(any())).thenReturn(savedRef);

        List<EventExternalRef> result = synchronizer.syncEvents(List.of(dto));

        assertThat(result)
            .isNotNull();
        verify(eventRepository).save(any());
        verify(refRepository).save(any());
    }

    @Test
    void throwsWhenProviderMismatch() {
        when(provider.getName()).thenReturn(ProviderName.UNKNOWN);
        List<ExternalEventDto> externalEventDtoList = List.of(dto);
        assertThrows(IllegalArgumentException.class,
            () -> synchronizer.syncEvents(externalEventDtoList));
    }
}
