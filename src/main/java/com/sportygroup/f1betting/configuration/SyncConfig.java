package com.sportygroup.f1betting.configuration;

import com.sportygroup.f1betting.entity.Provider;
import com.sportygroup.f1betting.entity.ProviderName;
import com.sportygroup.f1betting.properties.F1ApiProperties;
import com.sportygroup.f1betting.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Optional;

/**
 * Configuration for providing the active provider bean based on application properties.
 */
@Configuration
@RequiredArgsConstructor
public class SyncConfig {

    private final ProviderRepository providerRepository;
    private final F1ApiProperties properties;

    /**
     * Exposes the active Provider entity (from DB) as a primary bean.
     */
    @Bean("activeProvider")
    @Primary
    @ConditionalOnMissingBean(Provider.class)
    public Provider activeProvider() {
        return Optional.ofNullable(properties.getActiveProvider())
            .map(String::toUpperCase)
            .map(ProviderName::valueOf)
            .map(providerRepository::findByName)
            .orElseThrow(() -> new IllegalStateException("Active provider not specified in properties"))
            .orElseThrow(() -> new IllegalStateException("Provider not found in the database: %s"
                .formatted(properties.getActiveProvider().toUpperCase())));
    }
}
