package com.sportygroup.f1betting.repository;

import com.sportygroup.f1betting.entity.Bet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BetRepository extends JpaRepository<Bet, UUID> {
    Optional<Bet> findByUserIdAndEventOddsId(UUID userId, UUID eventOddsId);

    List<Bet> findByEventIdAndStatus(UUID eventId, String status);

    List<Bet> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
