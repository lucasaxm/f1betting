package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.entity.Driver;
import com.sportygroup.f1betting.entity.DriverExternalRef;
import com.sportygroup.f1betting.entity.EventExternalRef;
import com.sportygroup.f1betting.entity.Provider;
import com.sportygroup.f1betting.entity.ProviderName;
import com.sportygroup.f1betting.external.dto.ExternalDriverDto;
import com.sportygroup.f1betting.repository.DriverExternalRefRepository;
import com.sportygroup.f1betting.repository.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverSynchronizerTest {

    @Mock
    DriverRepository driverRepository;
    @Mock
    DriverExternalRefRepository refRepository;
    @Mock
    Provider provider;

    @InjectMocks
    DriverSynchronizer synchronizer;

    ExternalDriverDto dto;

    @BeforeEach
    void setup() {
        when(provider.getName()).thenReturn(ProviderName.OPENF1);
        dto = ExternalDriverDto.builder()
            .externalDriverId("1")
            .fullName("Lewis Hamilton")
            .externalEventId("e1")
            .providerName(ProviderName.OPENF1)
            .build();
    }

    @Test
    void syncDriverCreatesEntities() {
        when(driverRepository.findByFullNameIgnoreCase("Lewis Hamilton")).thenReturn(Optional.empty());
        when(refRepository.findByProviderAndExternalId(provider, "1")).thenReturn(Optional.empty());
        Driver savedDriver = new Driver();
        savedDriver.setId(UUID.randomUUID());
        when(driverRepository.save(any())).thenReturn(savedDriver);
        DriverExternalRef savedRef = new DriverExternalRef();
        savedRef.setId(UUID.randomUUID());
        when(refRepository.save(any())).thenReturn(savedRef);

        // Create a fake EventExternalRef for the test
        EventExternalRef eventRef = new EventExternalRef();
        eventRef.setExternalId("e1");

        // Prepare an input map for new signature
        var input = Collections.singletonMap(eventRef, List.of(dto));
        var result = synchronizer.syncDrivers(input);

        assertThat(result).containsKey(eventRef);
        assertThat(result.get(eventRef)).isNotNull();
        verify(driverRepository).save(any());
        verify(refRepository).save(any());
    }

    @Test
    void throwsWhenProviderMismatch() {
        when(provider.getName()).thenReturn(ProviderName.UNKNOWN);
        EventExternalRef eventRef = new EventExternalRef();
        eventRef.setExternalId("e1");
        var input = Collections.singletonMap(eventRef, List.of(dto));
        assertThrows(IllegalArgumentException.class,
            () -> synchronizer.syncDrivers(input));
    }
}
