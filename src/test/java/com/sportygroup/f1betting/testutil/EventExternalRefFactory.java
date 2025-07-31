package com.sportygroup.f1betting.testutil;

import com.sportygroup.f1betting.entity.Event;
import com.sportygroup.f1betting.entity.EventExternalRef;
import com.sportygroup.f1betting.entity.Provider;

public final class EventExternalRefFactory {
    private EventExternalRefFactory() {
    }

    public static EventExternalRef forEvent(Provider provider, Event event, String externalId) {
        EventExternalRef ref = new EventExternalRef();
        ref.setProvider(provider);
        ref.setEvent(event);
        ref.setExternalId(externalId);
        return ref;
    }
}
