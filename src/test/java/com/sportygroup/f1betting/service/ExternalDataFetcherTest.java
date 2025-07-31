package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.entity.EventExternalRef;
import com.sportygroup.f1betting.entity.ProviderName;
import com.sportygroup.f1betting.external.F1ExternalApi;
import com.sportygroup.f1betting.external.dto.ExternalDriverDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternalDataFetcherTest {

    @Mock
    F1ExternalApi f1ExternalApi;

    ExternalDataFetcher dataFetcher;

    @BeforeEach
    void setup() {
        dataFetcher = new ExternalDataFetcher(f1ExternalApi);
    }

    @Test
    void fetchDriversReturnsDriversPerEvent() {
        EventExternalRef eventRef1 = new EventExternalRef();
        eventRef1.setExternalId("1");
        EventExternalRef eventRef2 = new EventExternalRef();
        eventRef2.setExternalId("2");

        List<ExternalDriverDto> d1 = List.of(ExternalDriverDto.builder()
            .externalDriverId("d1")
            .fullName("Driver1")
            .externalEventId("1")
            .providerName(ProviderName.OPENF1)
            .build());
        List<ExternalDriverDto> d2 = List.of(ExternalDriverDto.builder()
            .externalDriverId("d2")
            .fullName("Driver2")
            .externalEventId("2")
            .providerName(ProviderName.OPENF1)
            .build());

        when(f1ExternalApi.listDrivers("1")).thenReturn(d1);
        when(f1ExternalApi.listDrivers("2")).thenReturn(d2);

        Map<EventExternalRef, List<ExternalDriverDto>> result = dataFetcher.fetchDrivers(List.of(eventRef1, eventRef2));

        assertThat(result).hasSize(2);
        assertThat(result.get(eventRef1)).containsExactlyElementsOf(d1);
        assertThat(result.get(eventRef2)).containsExactlyElementsOf(d2);
        verify(f1ExternalApi).listDrivers("1");
        verify(f1ExternalApi).listDrivers("2");
    }
}
