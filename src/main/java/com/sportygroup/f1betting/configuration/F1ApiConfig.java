package com.sportygroup.f1betting.configuration;

import com.sportygroup.f1betting.external.F1ExternalApi;
import com.sportygroup.f1betting.external.client.OpenF1Client;
import com.sportygroup.f1betting.properties.F1ApiProperties;
import com.sportygroup.f1betting.entity.ProviderName;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(F1ApiProperties.class)
public class F1ApiConfig {

    private final F1ApiProperties props;

    @Bean
    public F1ExternalApi f1ExternalApi(OpenF1Client openF1) {

        ProviderName provider;
        try {
            provider = ProviderName.valueOf(props.getActiveProvider().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Unknown provider: " + props.getActiveProvider());
        }

        return switch (provider) {
            case OPENF1 -> openF1;
            default -> throw new IllegalStateException("Provider not supported: " + provider);
        };
    }
}
