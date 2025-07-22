package com.sportygroup.f1betting.external.client;

import com.sportygroup.f1betting.exception.ExternalEventIdMissingException;
import com.sportygroup.f1betting.external.F1ExternalApi;
import com.sportygroup.f1betting.external.dto.DriverDto;
import com.sportygroup.f1betting.external.dto.ExternalEventDto;
import com.sportygroup.f1betting.external.dto.openf1.OpenF1DriverDto;
import com.sportygroup.f1betting.external.dto.openf1.OpenF1SessionDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class OpenF1Client implements F1ExternalApi {

    private final WebClient webClient;

    public OpenF1Client(@Value("${f1-external-api.providers.openf1.url}")
                        String baseUrl,
                        WebClient.Builder builder) {
        this.webClient = builder
            .baseUrl(baseUrl)
            .build();
    }

    @Override
    public List<ExternalEventDto> listEvents(Integer year,
                                             String type,
                                             String country) {

        return webClient.get()
            .uri(uri -> {
                uri.path("/v1/sessions");
                if (year != null) {
                    uri.queryParam("year", year);
                }
                if (type != null) {
                    uri.queryParam("session_type", type);
                }
                if (country != null) {
                    uri.queryParam("country_name", country);
                }
                return uri.build();
            })
            .retrieve()
            .bodyToFlux(OpenF1SessionDto.class)
            .map(OpenF1SessionDto::toEventSearchDto)
            .collectList()
            .block();                  // ok for your blocking service layer
    }

    @Override
    public List<DriverDto> listDrivers(String externalEventId) {

        if (externalEventId == null) {
            throw new ExternalEventIdMissingException();
        }

        return webClient.get()
            .uri(uri -> {
                uri.path("/v1/drivers");
                uri.queryParam("session_key", externalEventId);
                return uri.build();
            })
            .retrieve()
            .bodyToFlux(OpenF1DriverDto.class)
            .map(OpenF1DriverDto::toDriverDto)
            .collectList()
            .block();
    }
}
