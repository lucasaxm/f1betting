package com.sportygroup.f1betting.testutil;

import com.sportygroup.f1betting.entity.Event;
import com.sportygroup.f1betting.entity.EventType;

import java.time.OffsetDateTime;

public final class EventFactory {
    private EventFactory() {
    }

    public static Event belgiumRace2024() {
        Event event = new Event();
        event.setName("Race");
        event.setYear(2024);
        event.setCountry("Belgium");
        event.setType(EventType.RACE);
        event.setDateStart(OffsetDateTime.parse("2024-08-25T14:00:00Z"));
        return event;
    }

    public static Event belgiumRace2023() {
        Event event = new Event();
        event.setName("B Race");
        event.setYear(2023);
        event.setCountry("Belgium");
        event.setType(EventType.RACE);
        event.setDateStart(OffsetDateTime.parse("2023-08-27T14:00:00Z"));
        return event;
    }

    public static Event belgiumQualifying2024() {
        Event event = new Event();
        event.setName("Sprint");
        event.setYear(2024);
        event.setCountry("Belgium");
        event.setType(EventType.QUALIFYING);
        event.setDateStart(OffsetDateTime.parse("2024-08-26T14:00:00Z"));
        return event;
    }
}
