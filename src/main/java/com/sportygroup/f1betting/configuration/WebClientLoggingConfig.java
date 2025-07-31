package com.sportygroup.f1betting.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

@Configuration
@Slf4j
public class WebClientLoggingConfig {

    @Bean
    WebClientCustomizer loggingCustomizer() {
        return builder -> builder.filter(logFilter());
    }

    private ExchangeFilterFunction logFilter() {
        return (request, next) -> {
            long start = System.currentTimeMillis();
            return next.exchange(request)
                .doOnSuccess(resp -> log.info("HTTP {} {} -> {} ({} ms)",
                    request.method(), request.url(), resp.statusCode(),
                    System.currentTimeMillis() - start))
                .doOnError(err -> log.warn("HTTP {} {} -> {} ({} ms)",
                    request.method(), request.url(), err.toString(),
                    System.currentTimeMillis() - start));
        };
    }
}
