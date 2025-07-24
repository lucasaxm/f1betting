package com.sportygroup.f1betting.repository;

import com.sportygroup.f1betting.entity.Provider;
import com.sportygroup.f1betting.entity.SyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

public interface SyncStatusRepository extends JpaRepository<SyncStatus, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SyncStatus s where s.provider = :provider and s.year = :year")
    Optional<SyncStatus> findByProviderAndYearForUpdate(Provider provider, Integer year);

    Optional<SyncStatus> findByProviderAndYear(Provider provider, Integer year);
}
