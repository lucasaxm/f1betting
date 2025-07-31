package com.sportygroup.f1betting.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportygroup.f1betting.IntegrationTest;
import com.sportygroup.f1betting.entity.Driver;
import com.sportygroup.f1betting.entity.Event;
import com.sportygroup.f1betting.entity.EventOdd;
import com.sportygroup.f1betting.external.F1ExternalApi;
import com.sportygroup.f1betting.repository.BetRepository;
import com.sportygroup.f1betting.repository.DriverRepository;
import com.sportygroup.f1betting.repository.EventOddRepository;
import com.sportygroup.f1betting.repository.EventRepository;
import com.sportygroup.f1betting.repository.UserRepository;
import com.sportygroup.f1betting.testutil.DriverFactory;
import com.sportygroup.f1betting.testutil.EventFactory;
import com.sportygroup.f1betting.testutil.EventOddFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class BetControllerIntegrationTest {

    private static final UUID SEEDED_USER_ID = UUID.fromString("8c215643-ddbb-4cbe-8d92-fbd93f09018a");

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
    @Autowired
    BetRepository betRepository;
    @Autowired
    UserRepository userRepository;

    @MockitoBean(name = "f1ExternalApi")
    F1ExternalApi f1ExternalApi;

    static Stream<Arguments> invalidAmounts() {
        return Stream.of(
            Arguments.of(new BigDecimal("150"), "INSUFFICIENT_BALANCE"),
            Arguments.of(BigDecimal.ZERO, "INVALID_BET_AMOUNT"),
            Arguments.of(new BigDecimal("-5"), "INVALID_BET_AMOUNT")
        );
    }

    @BeforeEach
    void stubApi() {
        given(f1ExternalApi.listEvents(any(), any(), any())).willReturn(emptyList());
        given(f1ExternalApi.listDrivers(any())).willReturn(emptyList());
    }

    @Test
    void placeBetHappyPath() throws Exception {
        Event event = createEvent();
        Driver driver = createDriver();
        createEventOdd(event, driver, OffsetDateTime.now());

        String eventsJson = mockMvc.perform(get("/v1/events")
                .param("country", "Belgium"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        JsonNode events = objectMapper.readTree(eventsJson).get("content");
        String eventOddId = events.get(0).get("odds").get(0).get("id").asText();

        String betJson = betJson(SEEDED_USER_ID, UUID.fromString(eventOddId), new BigDecimal("25.50"));

        String response = mockMvc.perform(post("/v1/bets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(betJson))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        assertThat(root.get("remaining_balance").decimalValue())
            .isEqualByComparingTo(new BigDecimal("74.50"));
    }

    @ParameterizedTest
    @MethodSource("invalidAmounts")
    void invalidAmount(BigDecimal amount, String expectedError) throws Exception {
        Event event = createEvent();
        Driver driver = createDriver();
        EventOdd odd = createEventOdd(event, driver, OffsetDateTime.now());

        BigDecimal before = userRepository.findById(SEEDED_USER_ID).orElseThrow().getBalance();
        String betJson = betJson(SEEDED_USER_ID, odd.getId(), amount);

        String response = mockMvc.perform(post("/v1/bets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(betJson))
            .andExpect(status().isBadRequest())
            .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        assertThat(root.get("error").asText()).isEqualTo(expectedError);
        assertThat(betRepository.count()).isZero();
        assertThat(userRepository.findById(SEEDED_USER_ID).orElseThrow().getBalance())
            .isEqualByComparingTo(before);
    }

    @Test
    void eventClosed() throws Exception {
        Event event = createEvent();
        event.setWinnerDriverId(UUID.randomUUID());
        eventRepository.save(event);

        Driver driver = createDriver();
        EventOdd odd = createEventOdd(event, driver, OffsetDateTime.now());

        String betJson = betJson(SEEDED_USER_ID, odd.getId(), BigDecimal.TEN);

        BigDecimal before = userRepository.findById(SEEDED_USER_ID).orElseThrow().getBalance();

        String response = mockMvc.perform(post("/v1/bets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(betJson))
            .andExpect(status().isBadRequest())
            .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        assertThat(root.get("error").asText()).isEqualTo("EVENT_CLOSED");
        assertThat(betRepository.count()).isZero();
        assertThat(userRepository.findById(SEEDED_USER_ID).orElseThrow().getBalance())
            .isEqualByComparingTo(before);
    }

    @Test
    void outdatedEventOdd() throws Exception {
        Event event = createEvent();
        Driver driver = createDriver();
        EventOdd oldOdd = createEventOdd(event, driver, OffsetDateTime.now().minusDays(1));
        createEventOdd(event, driver, OffsetDateTime.now());

        String betJson = betJson(SEEDED_USER_ID, oldOdd.getId(), BigDecimal.TEN);

        BigDecimal before = userRepository.findById(SEEDED_USER_ID).orElseThrow().getBalance();

        String response = mockMvc.perform(post("/v1/bets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(betJson))
            .andExpect(status().isBadRequest())
            .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        assertThat(root.get("error").asText()).isEqualTo("OUTDATED_EVENT_ODD");
        assertThat(betRepository.count()).isZero();
        assertThat(userRepository.findById(SEEDED_USER_ID).orElseThrow().getBalance())
            .isEqualByComparingTo(before);
    }

    @Test
    void eventOddNotFound() throws Exception {
        String betJson = betJson(SEEDED_USER_ID, UUID.randomUUID(), BigDecimal.TEN);

        BigDecimal before = userRepository.findById(SEEDED_USER_ID).orElseThrow().getBalance();

        String response = mockMvc.perform(post("/v1/bets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(betJson))
            .andExpect(status().isNotFound())
            .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        assertThat(root.get("error").asText()).isEqualTo("EVENT_ODD_NOT_FOUND");
        assertThat(betRepository.count()).isZero();
        assertThat(userRepository.findById(SEEDED_USER_ID).orElseThrow().getBalance())
            .isEqualByComparingTo(before);
    }

    @Test
    void userNotFound() throws Exception {
        Event event = createEvent();
        Driver driver = createDriver();
        EventOdd odd = createEventOdd(event, driver, OffsetDateTime.now());

        UUID randomUser = UUID.randomUUID();

        String betJson = betJson(randomUser, odd.getId(), BigDecimal.TEN);

        String response = mockMvc.perform(post("/v1/bets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(betJson))
            .andExpect(status().isNotFound())
            .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        assertThat(root.get("error").asText()).isEqualTo("USER_NOT_FOUND");
        assertThat(betRepository.count()).isZero();
    }

    @Test
    void duplicateBetIdempotent() throws Exception {
        Event event = createEvent();
        Driver driver = createDriver();
        EventOdd odd = createEventOdd(event, driver, OffsetDateTime.now());

        String betJson = betJson(SEEDED_USER_ID, odd.getId(), new BigDecimal("25.50"));

        mockMvc.perform(post("/v1/bets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(betJson))
            .andExpect(status().isCreated());

        BigDecimal afterFirst = userRepository.findById(SEEDED_USER_ID).orElseThrow().getBalance();

        String response = mockMvc.perform(post("/v1/bets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(betJson))
            .andExpect(status().isConflict())
            .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        assertThat(root.get("error").asText()).isEqualTo("DUPLICATE_BET");
        assertThat(betRepository.count()).isEqualTo(1);
        assertThat(userRepository.findById(SEEDED_USER_ID).orElseThrow().getBalance())
            .isEqualByComparingTo(afterFirst);
    }

    private Event createEvent() {
        return eventRepository.save(EventFactory.belgiumRace2024());
    }

    private Driver createDriver() {
        return driverRepository.save(DriverFactory.sampleDriver());
    }

    private EventOdd createEventOdd(Event event, Driver driver, OffsetDateTime createdAt) {
        return eventOddRepository.save(EventOddFactory.with(event, driver, createdAt));
    }

    private String betJson(UUID userId, UUID oddId, BigDecimal amount) throws Exception {
        return objectMapper.writeValueAsString(new BetRequest(userId, oddId, amount));
    }

    private record BetRequest(UUID user_id, UUID event_odd_id, BigDecimal amount) {
    }

}
