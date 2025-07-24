package com.sportygroup.f1betting.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportygroup.f1betting.entity.Event;
import com.sportygroup.f1betting.entity.EventExternalRef;
import com.sportygroup.f1betting.entity.Provider;
import com.sportygroup.f1betting.external.dto.ExternalEventDto;
import com.sportygroup.f1betting.repository.EventExternalRefRepository;
import com.sportygroup.f1betting.repository.EventRepository;
import com.sportygroup.f1betting.repository.ProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.properties.hibernate.globally_quoted_identifiers=true",
    "f1-external-api.active-provider=openf1",
    "f1-external-api.providers.openf1.url=http://localhost",
    "f1-external-api.min-year=2020",
    "spring.main.allow-bean-definition-overriding=true"})
@AutoConfigureMockMvc
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

    @BeforeEach
    void setup() {
        eventExternalRefRepository.deleteAll();
        eventRepository.deleteAll();

        Provider provider = providerRepository.findByName("openf1").orElseThrow();

        Event e1 = new Event();
        e1.setName("B Race");
        e1.setYear(2023);
        e1.setCountry("Belgium");
        e1.setType("Race");
        e1.setDateStart(OffsetDateTime.parse("2023-08-27T14:00:00Z"));
        eventRepository.save(e1);
        EventExternalRef ref1 = new EventExternalRef();
        ref1.setProvider(provider);
        ref1.setExternalId("1");
        ref1.setEvent(e1);
        eventExternalRefRepository.save(ref1);

        Event e2 = new Event();
        e2.setName("A Race");
        e2.setYear(2024);
        e2.setCountry("Belgium");
        e2.setType("Race");
        e2.setDateStart(OffsetDateTime.parse("2024-08-25T14:00:00Z"));
        eventRepository.save(e2);
        EventExternalRef ref2 = new EventExternalRef();
        ref2.setProvider(provider);
        ref2.setExternalId("2");
        ref2.setEvent(e2);
        eventExternalRefRepository.save(ref2);
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
        assertThat(root.get("total_elements").asInt()).isEqualTo(2);
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

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public com.sportygroup.f1betting.external.F1ExternalApi f1ExternalApi() {
            return new com.sportygroup.f1betting.external.F1ExternalApi() {
                @Override
                public java.util.List<ExternalEventDto> listEvents(Integer year,
                                                                   String type,
                                                                   String country) {
                    return java.util.Collections.emptyList();
                }

                @Override
                public java.util.List<com.sportygroup.f1betting.external.dto.DriverDto> listDrivers(
                    String externalEventId) {
                    return java.util.Collections.emptyList();
                }
            };
        }

        @Bean
        @Primary
        public com.sportygroup.f1betting.service.SyncService syncService(
            com.sportygroup.f1betting.repository.SyncStatusRepository syncStatusRepository,
            com.sportygroup.f1betting.repository.EventRepository eventRepository,
            com.sportygroup.f1betting.repository.EventExternalRefRepository eventExternalRefRepository,
            com.sportygroup.f1betting.repository.ProviderRepository providerRepository,
            com.sportygroup.f1betting.external.F1ExternalApi api,
            com.sportygroup.f1betting.properties.F1ApiProperties props) {
            Provider provider = new Provider();
            provider.setName("openf1");
            providerRepository.save(provider);
            return new com.sportygroup.f1betting.service.SyncService(syncStatusRepository,
                eventRepository,
                eventExternalRefRepository,
                providerRepository,
                api,
                props);
        }
    }
}
