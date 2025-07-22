package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.properties.F1ApiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Year;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class StartupSyncRunner implements ApplicationRunner {

    private final SyncService syncService;
    private final F1ApiProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        int current = Year.now().getValue();
        for (int y = properties.getMinYear(); y < current; y++) {
            try {
                syncService.syncYear(y);
            } catch (Exception ignored) {
                // past-year sync failures shouldn't block startup
            }
        }
    }
}
