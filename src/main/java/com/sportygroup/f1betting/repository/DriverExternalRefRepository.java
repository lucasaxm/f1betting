package com.sportygroup.f1betting.repository;

import com.sportygroup.f1betting.entity.DriverExternalRef;
import com.sportygroup.f1betting.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DriverExternalRefRepository extends JpaRepository<DriverExternalRef, UUID> {
    Optional<DriverExternalRef> findByProviderAndExternalId(Provider provider, String externalId);
}
