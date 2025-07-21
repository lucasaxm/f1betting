package com.sportygroup.f1betting.external.dto.openf1;

import com.sportygroup.f1betting.external.dto.DriverDto;

public record OpenF1DriverDto(String driverNumber,
                              String fullName,
                              String shortName,
                              String countryCode,
                              String sessionKey) {
    public DriverDto toDriverDto() {
        return DriverDto.builder()
            .externalDriverId(this.driverNumber)
            .fullName(this.fullName)
            .externalEventId(this.sessionKey)
            .build();
    }
}
