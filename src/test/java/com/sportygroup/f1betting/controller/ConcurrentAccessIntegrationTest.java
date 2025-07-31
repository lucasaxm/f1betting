package com.sportygroup.f1betting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportygroup.f1betting.IntegrationTest;
import com.sportygroup.f1betting.entity.Driver;
import com.sportygroup.f1betting.entity.Event;
import com.sportygroup.f1betting.entity.EventOdd;
import com.sportygroup.f1betting.external.F1ExternalApi;
import com.sportygroup.f1betting.repository.*;
import com.sportygroup.f1betting.testutil.DriverFactory;
import com.sportygroup.f1betting.testutil.EventFactory;
import com.sportygroup.f1betting.testutil.EventOddFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class ConcurrentAccessIntegrationTest {

    private static final UUID USER = UUID.fromString("8c215643-ddbb-4cbe-8d92-fbd93f09018a");

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

    @BeforeEach
    void stubApi() {
        given(f1ExternalApi.listEvents(any(), any(), any())).willReturn(emptyList());
        given(f1ExternalApi.listDrivers(any())).willReturn(emptyList());
    }

    @Test
    void betAndOutcomeSerializedOnSameEvent() throws Exception {
        Event event = eventRepository.save(EventFactory.belgiumRace2024());
        Driver driver = driverRepository.save(DriverFactory.sampleDriver());
        EventOdd odd = eventOddRepository.save(EventOddFactory.with(event, driver, OffsetDateTime.now()));

        String betJson = objectMapper.writeValueAsString(new BetRequest(USER, odd.getId(), BigDecimal.TEN));
        String outcomeJson = objectMapper.writeValueAsString(new OutcomeRequest(driver.getId()));

        CountDownLatch latch = new CountDownLatch(1);
        ExecutorService exec = Executors.newFixedThreadPool(2);

        Callable<Integer> betCall = () -> {
            latch.await();
            return mockMvc.perform(post("/v1/bets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(betJson))
                .andReturn().getResponse().getStatus();
        };

        Callable<Integer> outcomeCall = () -> {
            latch.await();
            return mockMvc.perform(post("/v1/events/" + event.getId() + "/outcome")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(outcomeJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getStatus();
        };

        Future<Integer> betFuture = exec.submit(betCall);
        Future<Integer> outcomeFuture = exec.submit(outcomeCall);
        latch.countDown();

        int betStatus = betFuture.get();
        int outcomeStatus = outcomeFuture.get();
        exec.shutdown();

        assertThat(outcomeStatus).isEqualTo(200);
        assertThat(eventRepository.findById(event.getId()).orElseThrow().getWinnerDriverId())
            .isEqualTo(driver.getId());
        assertThat(betRepository.findByEventIdAndStatus(event.getId(), "PENDING")).isEmpty();

        if (betStatus == 201) {
            assertThat(betRepository.count()).isEqualTo(1);
        } else {
            assertThat(betStatus).isEqualTo(400);
            assertThat(betRepository.count()).isZero();
        }
    }

    private record BetRequest(UUID user_id, UUID event_odd_id, BigDecimal amount) {}
    private record OutcomeRequest(UUID winner_driver_id) {}
}
