package com.sportygroup.f1betting.external.dto.openf1;

import com.sportygroup.f1betting.entity.ProviderName;
import com.sportygroup.f1betting.external.dto.ExternalDriverDto;

import java.util.ArrayList;
import java.util.List;

public record OpenF1DriverDto(String driverNumber,
                              String fullName,
                              String firstName,
                              String lastName,
                              String shortName,
                              String countryCode,
                              String sessionKey) {
    public ExternalDriverDto toDriverDto() {
        var fullName = buildFullName();
        return ExternalDriverDto.builder()
            .externalDriverId(this.driverNumber)
            .fullName(fullName)
            .externalEventId(this.sessionKey)
            .providerName(ProviderName.OPENF1)
            .build();
    }

    private String buildFullName() {
        if (this.firstName == null || this.lastName == null) {
            // If firstName or lastName is null, use fullName directly
            return this.fullName.toLowerCase();
        } else {
            var actualFirstName = this.firstName.split(" ")[0];
            var actualLastName = new ArrayList<>(List.of(this.lastName.split(" "))).getLast();
            return "%s %s".formatted(actualFirstName, actualLastName).toLowerCase();
        }
    }
}
