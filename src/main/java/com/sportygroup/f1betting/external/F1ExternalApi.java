package com.sportygroup.f1betting.external;

import com.sportygroup.f1betting.external.dto.DriverDto;
import com.sportygroup.f1betting.external.dto.EventDto;

import java.util.List;

/**
 * Abstraction of whatever provider is active (OpenF1 now, Ergast later, …).
 */
public interface F1ExternalApi {

    List<EventDto> listEvents(Integer year,
                              String type,   // "Race", "Sprint", …
                              String country);      // "Belgium", …

    List<DriverDto> listDrivers(String externalEventId);       // optional ‑ pass null for “all”
}
