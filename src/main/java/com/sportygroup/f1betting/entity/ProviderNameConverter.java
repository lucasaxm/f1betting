package com.sportygroup.f1betting.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ProviderNameConverter implements AttributeConverter<ProviderName, String> {
    @Override
    public String convertToDatabaseColumn(ProviderName attribute) {
        return attribute == null ? null : attribute.name().toLowerCase();
    }

    @Override
    public ProviderName convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ProviderName.valueOf(dbData.toUpperCase());
    }
}
