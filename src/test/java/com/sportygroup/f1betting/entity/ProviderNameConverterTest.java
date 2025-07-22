package com.sportygroup.f1betting.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProviderNameConverterTest {

    ProviderNameConverter converter = new ProviderNameConverter();

    @Test
    void convertsToLowerCase() {
        assertEquals("openf1", converter.convertToDatabaseColumn(ProviderName.OPENF1));
    }

    @Test
    void convertsFromString() {
        assertEquals(ProviderName.ERGAST, converter.convertToEntityAttribute("ergast"));
    }
}
