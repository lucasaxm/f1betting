package com.sportygroup.f1betting.repository;

import com.sportygroup.f1betting.entity.EventExternalRef;
import com.sportygroup.f1betting.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EventExternalRefRepository extends JpaRepository<EventExternalRef, UUID> {
    Optional<EventExternalRef> findByProviderAndExternalId(Provider provider, String externalId);
}
