package com.sportygroup.f1betting.testutil;

import com.sportygroup.f1betting.entity.Driver;
import com.sportygroup.f1betting.entity.Event;
import com.sportygroup.f1betting.entity.EventOdd;

import java.time.OffsetDateTime;

public final class EventOddFactory {
    private EventOddFactory() {
    }

    public static EventOdd with(Event event, Driver driver, OffsetDateTime createdAt) {
        EventOdd odd = new EventOdd();
        odd.setEvent(event);
        odd.setDriver(driver);
        odd.setOdd((short) 3);
        odd.setCreatedAt(createdAt != null ? createdAt : OffsetDateTime.now());
        return odd;
    }
}
