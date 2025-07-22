package com.sportygroup.f1betting.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ProviderName {
    OPENF1,
    ERGAST;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }
}
