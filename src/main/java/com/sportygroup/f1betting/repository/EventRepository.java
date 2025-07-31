package com.sportygroup.f1betting.repository;

import com.sportygroup.f1betting.entity.Event;
import com.sportygroup.f1betting.entity.EventType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

    @Query("""
        select e.id
        from   Event e
        where  (:year    is null or e.year    = :year)
          and  (:type    is null or e.type    = :type)
          and  (:country is null or lower(e.country) = :country)
        """)
    Page<UUID> findIdsByFilter(Integer year, EventType type, String country, Pageable pageable);


    @Query("""
        select distinct e
        from   Event e
           join fetch e.eventOdds o
           join fetch o.driver      d
           join       CurrentEventOddsView v
                 on   v.id      = o.id
                and   v.eventId = e.id
        where  e.id in :ids
        """)
    List<Event> findAllWithCurrentOddsByIdIn(Collection<UUID> ids);

    Optional<Event> findByCountryIgnoreCaseAndDateStart(String country, OffsetDateTime dateStart);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Event e where e.id = :id")
    Optional<Event> findByIdForUpdate(UUID id);
}
