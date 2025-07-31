package com.sportygroup.f1betting.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "name", nullable = false, length = Integer.MAX_VALUE)
    private String name;

    @NotNull
    @Column(name = "year", nullable = false)
    private Integer year;

    @Size(max = 100)
    @NotNull
    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private EventType type;

    @NotNull
    @Column(name = "date_start", nullable = false)
    private OffsetDateTime dateStart;

    @Column(name = "winner_driver_id")
    private UUID winnerDriverId;

    @OneToMany(mappedBy = "event")
    private Set<Bet> bets = new LinkedHashSet<>();

    @OneToMany(mappedBy = "event")
    private Set<EventExternalRef> eventExternalRefs = new LinkedHashSet<>();

    @OneToMany(mappedBy = "event")
    private Set<EventOdd> eventOdds = new LinkedHashSet<>();

}
