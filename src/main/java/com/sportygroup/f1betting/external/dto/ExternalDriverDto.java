package com.sportygroup.f1betting.external.dto;

import com.sportygroup.f1betting.entity.ProviderName;
import lombok.Builder;

@Builder
public record ExternalDriverDto(String externalDriverId,
                                String fullName,
                                String externalEventId,
                                ProviderName providerName) {
}
