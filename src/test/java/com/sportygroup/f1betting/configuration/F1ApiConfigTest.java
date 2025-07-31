package com.sportygroup.f1betting.configuration;

import com.sportygroup.f1betting.external.client.OpenF1Client;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class F1ApiConfigTest {

    ApplicationContextRunner runner = new ApplicationContextRunner()
        .withUserConfiguration(F1ApiConfig.class)
        .withBean(OpenF1Client.class, () -> new OpenF1Client("http://localhost", WebClient.builder()));

    @Test
    void createsOpenF1ClientBean() {
        runner.withPropertyValues("f1-external-api.active-provider=openf1")
            .run(context -> {
                assertThat(context).hasBean("f1ExternalApi");
                assertThat(context.getBean("f1ExternalApi")).isInstanceOf(OpenF1Client.class);
            });
    }

    @Test
    void unknownProviderCausesFailure() {
        runner.withPropertyValues("f1-external-api.active-provider=unknown")
            .run(context -> {
                assertThat(context.getStartupFailure()).isNotNull();
                assertThat(context.getStartupFailure().getMessage()).contains("Provider not supported");
            });
    }
}
