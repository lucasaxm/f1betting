package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.entity.Driver;
import com.sportygroup.f1betting.entity.DriverExternalRef;
import com.sportygroup.f1betting.entity.EventExternalRef;
import com.sportygroup.f1betting.entity.Provider;
import com.sportygroup.f1betting.external.dto.ExternalDriverDto;
import com.sportygroup.f1betting.repository.DriverExternalRefRepository;
import com.sportygroup.f1betting.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverSynchronizer {
    private final DriverRepository driverRepository;
    private final DriverExternalRefRepository driverExternalRefRepository;
    private final Provider provider;

    public Map<EventExternalRef, List<DriverExternalRef>> syncDrivers(
        Map<EventExternalRef, List<ExternalDriverDto>> driversDtoByEventRef) {
        Map<String, DriverExternalRef> externalRefByExternalId = new HashMap<>();
        Map<EventExternalRef, List<DriverExternalRef>> driversRefByEventRef = new HashMap<>();

        driversDtoByEventRef.forEach((eventRef, externalDriverDtos) -> {
            log.info("Syncing drivers for event: {}", eventRef.getExternalId());
            List<DriverExternalRef> driverRefs = externalDriverDtos.stream().map(
                externalDriverDto -> externalRefByExternalId.compute(externalDriverDto.externalDriverId(),
                    (key, existingRef) -> {
                        if (existingRef == null) {
                            log.info("Syncing driver: {}", externalDriverDto.externalDriverId());
                            return upsertDriver(externalDriverDto);
                        } else {
                            log.warn("Duplicate driver found: {}", externalDriverDto.externalDriverId());
                            return existingRef;
                        }
                    })).toList();
            driversRefByEventRef.put(eventRef, driverRefs);
            log.info("Synced {} drivers for event: {}", driverRefs.size(), eventRef.getExternalId());
        });
        return driversRefByEventRef;

    }

    private DriverExternalRef upsertDriver(ExternalDriverDto dto) {
        log.info("Upserting driver: {}", dto.externalDriverId());
        checkProvider(dto);
        Driver driver = driverRepository
            .findByFullNameIgnoreCase(dto.fullName())
            .orElseGet(Driver::new);
        DriverExternalRef ref = driverExternalRefRepository
            .findByProviderAndExternalId(provider, dto.externalDriverId())
            .orElseGet(DriverExternalRef::new);

        driver.setFullName(dto.fullName());
        driverRepository.save(driver);
        ref.setProvider(provider);
        ref.setExternalId(dto.externalDriverId());
        ref.setDriver(driver);

        return driverExternalRefRepository.save(ref);
    }

    private void checkProvider(ExternalDriverDto dto) {
        if (!dto.providerName().equals(provider.getName())) {
            log.error("Driver {} from provider {} does not match the active provider {}",
                dto.fullName(), dto.providerName(), provider.getName());
            throw new IllegalArgumentException("Driver provider does not match the active provider");
        }
    }
}
