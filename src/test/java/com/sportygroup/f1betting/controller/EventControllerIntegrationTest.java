package com.sportygroup.f1betting.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportygroup.f1betting.IntegrationTest;
import com.sportygroup.f1betting.entity.Event;
import com.sportygroup.f1betting.entity.EventType;
import com.sportygroup.f1betting.entity.ProviderName;
import com.sportygroup.f1betting.external.F1ExternalApi;
import com.sportygroup.f1betting.repository.DriverRepository;
import com.sportygroup.f1betting.repository.EventExternalRefRepository;
import com.sportygroup.f1betting.repository.EventOddRepository;
import com.sportygroup.f1betting.repository.EventRepository;
import com.sportygroup.f1betting.repository.ProviderRepository;
import com.sportygroup.f1betting.testutil.DriverFactory;
import com.sportygroup.f1betting.testutil.EventExternalRefFactory;
import com.sportygroup.f1betting.testutil.EventFactory;
import com.sportygroup.f1betting.testutil.EventOddFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class EventControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    EventRepository eventRepository;
    @Autowired
    EventExternalRefRepository eventExternalRefRepository;
    @Autowired
    ProviderRepository providerRepository;
    @Autowired
    DriverRepository driverRepository;
    @Autowired
    EventOddRepository eventOddRepository;

    @MockitoBean(name = "f1ExternalApi")
    F1ExternalApi f1ExternalApi;

    @BeforeEach
    void stubApi() {
        given(f1ExternalApi.listEvents(any(), any(), any())).willReturn(emptyList());
        given(f1ExternalApi.listDrivers(any())).willReturn(emptyList());
    }

    @BeforeEach
    void setup() {
        var provider = providerRepository.findByName(ProviderName.OPENF1).orElseThrow();

        Event e1 = eventRepository.save(EventFactory.belgiumRace2023());
        eventExternalRefRepository.save(EventExternalRefFactory.forEvent(provider, e1, "1"));
        eventOddRepository.save(
            EventOddFactory.with(e1, driverRepository.save(DriverFactory.sampleDriver()), OffsetDateTime.now()));

        Event e2 = eventRepository.save(EventFactory.belgiumRace2024());
        eventExternalRefRepository.save(EventExternalRefFactory.forEvent(provider, e2, "2"));
        eventOddRepository.save(
            EventOddFactory.with(e2, driverRepository.save(DriverFactory.sampleDriver()), OffsetDateTime.now()));

        Event e3 = eventRepository.save(EventFactory.belgiumQualifying2024());
        eventExternalRefRepository.save(EventExternalRefFactory.forEvent(provider, e3, "3"));
        eventOddRepository.save(
            EventOddFactory.with(e3, driverRepository.save(DriverFactory.sampleDriver()), OffsetDateTime.now()));
    }

    @Test
    void getEventsReturnsSortedPageWithDateStart() throws Exception {
        String json = mockMvc.perform(get("/v1/events")
                .param("country", "Belgium")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("total_elements").asInt()).isEqualTo(3);
        assertThat(root.get("total_pages").asInt()).isEqualTo(1);
        JsonNode content = root.get("content");
        assertThat(content.get(0).get("year").asInt()).isEqualTo(2024);
        assertThat(content.get(0).get("date_start").asText()).isNotEmpty();
    }

    @Test
    void sortByDateStartAscending() throws Exception {
        String json = mockMvc.perform(get("/v1/events")
                .param("country", "Belgium")
                .param("sort", "date_start,asc"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        JsonNode content = objectMapper.readTree(json).get("content");
        assertThat(content.get(0).get("year").asInt()).isEqualTo(2023);
    }

    @Test
    void unknownSortFieldReturns400() throws Exception {
        String json = mockMvc.perform(get("/v1/events")
                .param("sort", "foo,asc"))
            .andExpect(status().isBadRequest())
            .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("error").asText()).isEqualTo("INVALID_SORT_FIELD");
    }

    @Test
    void filterByYearAndType() throws Exception {
        String json = mockMvc.perform(get("/v1/events")
                .param("country", "Belgium")
                .param("year", "2024")
                .param("type", EventType.QUALIFYING.name()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        JsonNode content = objectMapper.readTree(json).get("content");
        assertThat(content).hasSize(1);
        assertThat(content.get(0).get("event_type").asText()).isEqualTo(EventType.QUALIFYING.name());
    }

    @Test
    void emptyResultReturnsEmptyPage() throws Exception {
        String json = mockMvc.perform(get("/v1/events")
                .param("year", "1990"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("total_elements").asInt()).isZero();
    }

}
