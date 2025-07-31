package com.sportygroup.f1betting.repository;

import com.sportygroup.f1betting.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DriverRepository extends JpaRepository<Driver, UUID> {
    Optional<Driver> findByFullNameIgnoreCase(String fullName);
}
