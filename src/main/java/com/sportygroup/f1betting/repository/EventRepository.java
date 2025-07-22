package com.sportygroup.f1betting.repository;

import com.sportygroup.f1betting.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

    @Query("""
            select e from Event e
            where (:year is null or e.year = :year)
              and (:type is null or e.type = :type)
              and (:country is null or e.country = :country)
            """)
    Page<Event> findByFilter(Integer year, String type, String country, Pageable pageable);

    Optional<Event> findByCountryAndDateStart(String country, OffsetDateTime dateStart);
}
