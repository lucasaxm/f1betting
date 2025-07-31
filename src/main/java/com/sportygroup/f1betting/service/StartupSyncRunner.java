package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.properties.F1ApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Year;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class StartupSyncRunner implements ApplicationRunner {

    private final SyncService syncService;
    private final F1ApiProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        int current = Year.now().getValue();
        log.info("Startup sync from {} to {}", properties.getMinYear(), current - 1);
        for (int y = properties.getMinYear(); y <= current; y++) {
            try {
                log.info("Syncing year {}", y);
                syncService.syncYear(y);
            } catch (Exception e) {
                log.warn("Failed to sync year {}", y, e);
            }
        }
        log.info("Startup sync completed");
    }
}
