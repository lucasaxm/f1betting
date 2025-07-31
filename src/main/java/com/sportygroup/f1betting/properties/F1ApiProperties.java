package com.sportygroup.f1betting.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties("f1-external-api")
public class F1ApiProperties {

    private String activeProvider;
    private Integer minYear;
    private Map<String, ProviderConfig> providers = new HashMap<>();

    @Getter
    @Setter
    public static class ProviderConfig {
        private String url;
    }
}
