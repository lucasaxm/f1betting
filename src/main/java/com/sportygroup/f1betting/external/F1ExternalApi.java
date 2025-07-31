package com.sportygroup.f1betting.external;

import com.sportygroup.f1betting.external.dto.ExternalDriverDto;
import com.sportygroup.f1betting.external.dto.ExternalEventDto;

import java.util.List;

/**
 * Abstraction of whatever provider is active (OpenF1 now, Ergast later, …).
 */
public interface F1ExternalApi {

    List<ExternalEventDto> listEvents(Integer year,
                                      String type,
                                      String country);

    List<ExternalDriverDto> listDrivers(String externalEventId);       // optional ‑ pass null for “all”
}
