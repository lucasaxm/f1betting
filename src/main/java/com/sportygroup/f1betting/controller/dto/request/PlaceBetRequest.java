package com.sportygroup.f1betting.controller.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public record PlaceBetRequest(
    UUID userId,
    UUID eventOddId,
    BigDecimal amount
) {}
