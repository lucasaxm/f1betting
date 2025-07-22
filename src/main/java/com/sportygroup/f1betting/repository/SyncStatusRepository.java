package com.sportygroup.f1betting.repository;

import com.sportygroup.f1betting.entity.SyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface SyncStatusRepository extends JpaRepository<SyncStatus, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SyncStatus s where s.year = :year")
    Optional<SyncStatus> findByYearForUpdate(Integer year);
}
