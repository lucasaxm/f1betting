package com.sportygroup.f1betting.repository;

import com.sportygroup.f1betting.entity.CurrentEventOddsView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CurrentEventOddsViewRepository extends JpaRepository<CurrentEventOddsView, UUID> {
}

