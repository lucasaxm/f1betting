package com.sportygroup.f1betting.repository;

import com.sportygroup.f1betting.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProviderRepository extends JpaRepository<Provider, UUID> {
    Optional<Provider> findByName(String name);
}
