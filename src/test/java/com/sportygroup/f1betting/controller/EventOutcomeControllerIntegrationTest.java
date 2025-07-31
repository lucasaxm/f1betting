package com.sportygroup.f1betting.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportygroup.f1betting.IntegrationTest;
import com.sportygroup.f1betting.entity.Driver;
import com.sportygroup.f1betting.entity.Event;
import com.sportygroup.f1betting.entity.EventOdd;
import com.sportygroup.f1betting.entity.Bet;
import com.sportygroup.f1betting.repository.*;
import com.sportygroup.f1betting.testutil.DriverFactory;
import com.sportygroup.f1betting.testutil.EventFactory;
import com.sportygroup.f1betting.testutil.EventOddFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.sportygroup.f1betting.external.F1ExternalApi;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.ServletException;

@IntegrationTest
class EventOutcomeControllerIntegrationTest {

    private static final UUID USER1 = UUID.fromString("8c215643-ddbb-4cbe-8d92-fbd93f09018a");
    private static final UUID USER2 = UUID.fromString("baa6fe5d-adf0-4a17-8258-b1fcadb6646b");

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    EventRepository eventRepository;
    @Autowired
    DriverRepository driverRepository;
    @Autowired
    EventOddRepository eventOddRepository;
    @MockitoSpyBean
    BetRepository betRepository;
    @Autowired
    UserRepository userRepository;

    @MockitoBean(name = "f1ExternalApi")
    F1ExternalApi f1ExternalApi;

    @BeforeEach
    void stubApi() {
        given(f1ExternalApi.listEvents(any(), any(), any())).willReturn(emptyList());
        given(f1ExternalApi.listDrivers(any())).willReturn(emptyList());
    }

    @Test
    void declareOutcomeHappyPath() throws Exception {
        Event event = eventRepository.save(EventFactory.belgiumRace2024());
        Driver d1 = driverRepository.save(DriverFactory.sampleDriver());
        Driver d2 = driverRepository.save(DriverFactory.sampleDriver());
        EventOdd o1 = eventOddRepository.save(EventOddFactory.with(event, d1, OffsetDateTime.now()));
        EventOdd o2 = eventOddRepository.save(EventOddFactory.with(event, d2, OffsetDateTime.now()));

        placeBet(USER1, o1.getId(), new BigDecimal("10"));
        placeBet(USER2, o2.getId(), new BigDecimal("20"));

        MvcResult result = mockMvc.perform(post("/v1/events/" + event.getId() + "/outcome")
                .contentType(MediaType.APPLICATION_JSON)
                .content(outcomeJson(d1.getId())))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.get("bets_settled").asInt()).isEqualTo(2);

        assertThat(eventRepository.findById(event.getId()).orElseThrow().getWinnerDriverId())
            .isEqualTo(d1.getId());
        assertThat(betRepository.findAll().stream().filter(b -> b.getStatus().equals("WON")).count()).isEqualTo(1);
        assertThat(userRepository.findById(USER1).orElseThrow().getBalance())
            .isEqualByComparingTo(new BigDecimal("120.00"));
        assertThat(userRepository.findById(USER2).orElseThrow().getBalance())
            .isEqualByComparingTo(new BigDecimal("80.00"));
    }

    @Test
    void outcomeWithNoBets() throws Exception {
        Event event = eventRepository.save(EventFactory.belgiumRace2024());
        Driver d1 = driverRepository.save(DriverFactory.sampleDriver());
        eventOddRepository.save(EventOddFactory.with(event, d1, OffsetDateTime.now()));

        MvcResult result = mockMvc.perform(post("/v1/events/" + event.getId() + "/outcome")
                .contentType(MediaType.APPLICATION_JSON)
                .content(outcomeJson(d1.getId())))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.get("bets_settled").asInt()).isZero();
        assertThat(eventRepository.findById(event.getId()).orElseThrow().getWinnerDriverId())
            .isEqualTo(d1.getId());
    }

    @Test
    void outcomeAlreadyRecorded() throws Exception {
        Event event = eventRepository.save(EventFactory.belgiumRace2024());
        Driver d1 = driverRepository.save(DriverFactory.sampleDriver());
        event.setWinnerDriverId(UUID.randomUUID());
        eventRepository.save(event);
        eventOddRepository.save(EventOddFactory.with(event, d1, OffsetDateTime.now()));

        MvcResult result = mockMvc.perform(post("/v1/events/" + event.getId() + "/outcome")
                .contentType(MediaType.APPLICATION_JSON)
                .content(outcomeJson(d1.getId())))
            .andExpect(status().isConflict())
            .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.get("error").asText()).isEqualTo("EVENT_ALREADY_CLOSED");
    }

    @Test
    void driverNotInEvent() throws Exception {
        Event event = eventRepository.save(EventFactory.belgiumRace2024());
        Driver d1 = driverRepository.save(DriverFactory.sampleDriver());
        Driver d2 = driverRepository.save(DriverFactory.sampleDriver());
        eventOddRepository.save(EventOddFactory.with(event, d1, OffsetDateTime.now()));

        MvcResult result = mockMvc.perform(post("/v1/events/" + event.getId() + "/outcome")
                .contentType(MediaType.APPLICATION_JSON)
                .content(outcomeJson(d2.getId())))
            .andExpect(status().isBadRequest())
            .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.get("error").asText()).isEqualTo("DRIVER_NOT_IN_EVENT");
    }

    @Test
    void eventNotFound() throws Exception {
        UUID missingEvent = UUID.randomUUID();
        UUID driver = UUID.randomUUID();

        MvcResult result = mockMvc.perform(post("/v1/events/" + missingEvent + "/outcome")
                .contentType(MediaType.APPLICATION_JSON)
                .content(outcomeJson(driver)))
            .andExpect(status().isNotFound())
            .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.get("error").asText()).isEqualTo("EVENT_NOT_FOUND");
    }

    @Test
    void driverNotFound() throws Exception {
        Event event = eventRepository.save(EventFactory.belgiumRace2024());
        eventOddRepository.save(EventOddFactory.with(event, driverRepository.save(DriverFactory.sampleDriver()), OffsetDateTime.now()));

        UUID missingDriver = UUID.randomUUID();
        MvcResult result = mockMvc.perform(post("/v1/events/" + event.getId() + "/outcome")
                .contentType(MediaType.APPLICATION_JSON)
                .content(outcomeJson(missingDriver)))
            .andExpect(status().isNotFound())
            .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.get("error").asText()).isEqualTo("DRIVER_NOT_FOUND");
    }

    @Test
    void transactionalRollbackOnError() throws Exception {
        Event event = eventRepository.save(EventFactory.belgiumRace2024());
        Driver d1 = driverRepository.save(DriverFactory.sampleDriver());
        EventOdd o1 = eventOddRepository.save(EventOddFactory.with(event, d1, OffsetDateTime.now()));
        placeBet(USER1, o1.getId(), new BigDecimal("10"));

        doThrow(new RuntimeException("fail")).when(betRepository).save(any(Bet.class));

        try {
            mockMvc.perform(post("/v1/events/" + event.getId() + "/outcome")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(outcomeJson(d1.getId())))
                .andExpect(status().isInternalServerError());
        } catch (ServletException ignored) {
            // expected
        }

        assertThat(eventRepository.findById(event.getId()).orElseThrow().getWinnerDriverId()).isNull();
        assertThat(betRepository.findAll().getFirst().getStatus()).isEqualTo("PENDING");
        assertThat(userRepository.findById(USER1).orElseThrow().getBalance())
            .isEqualByComparingTo(new BigDecimal("90.00"));
    }

    private void placeBet(UUID userId, UUID oddId, BigDecimal amount) throws Exception {
        String json = objectMapper.writeValueAsString(new BetRequest(userId, oddId, amount));
        mockMvc.perform(post("/v1/bets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated());
    }

    private String outcomeJson(UUID driverId) throws Exception {
        return objectMapper.writeValueAsString(new OutcomeRequest(driverId));
    }

    private record BetRequest(UUID user_id, UUID event_odd_id, BigDecimal amount) {
    }

    private record OutcomeRequest(UUID winner_driver_id) {
    }
}
