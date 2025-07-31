package com.sportygroup.f1betting.external.client;

import com.sportygroup.f1betting.exception.ExternalEventIdMissingException;
import com.sportygroup.f1betting.external.dto.ExternalDriverDto;
import com.sportygroup.f1betting.external.dto.ExternalEventDto;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OpenF1ClientTest {

    MockWebServer server;
    OpenF1Client client;

    @BeforeEach
    void setup() throws Exception {
        server = new MockWebServer();
        server.start();
        client = new OpenF1Client(server.url("/").toString(), WebClient.builder());
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void listDriversRequiresEventId() {
        assertThrows(ExternalEventIdMissingException.class, () -> client.listDrivers(null));
    }

    @Test
    void listEventsParsesResponse() {
        String body = """
            [{
                "sessionKey":"1",
                "sessionName":"Race",
                "sessionType":"Race",
                "year":2024,
                "countryName":"BE",
                "dateStart":"2024-08-25T14:00:00Z",
                "dateEnd":"2024-08-25T16:00:00Z"
            }]
            """;
        server.enqueue(new MockResponse().setBody(body).setHeader("Content-Type", "application/json"));

        List<ExternalEventDto> result = client.listEvents(2024, "Race", "BE");

        assertThat(result).hasSize(1);
        ExternalEventDto dto = result.getFirst();
        assertThat(dto.externalEventId()).isEqualTo("1");
        assertThat(dto.countryName()).isEqualTo("BE");
    }

    @Test
    void listDriversParsesResponse() {
        String body = """
            [{
                "fullName":"Driver",
                "driver_number":"1",
                "session_key":"1"
            }]
            """;
        server.enqueue(new MockResponse().setBody(body).setHeader("Content-Type", "application/json"));

        List<ExternalDriverDto> result = client.listDrivers("1");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().fullName()).isEqualTo("driver");
    }
}
