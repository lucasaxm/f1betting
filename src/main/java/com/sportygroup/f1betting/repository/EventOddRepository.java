package com.sportygroup.f1betting.repository;

import com.sportygroup.f1betting.entity.EventOdd;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventOddRepository extends JpaRepository<EventOdd, UUID> {
    boolean existsByEventIdAndDriverId(UUID eventId, UUID driverId);
}
