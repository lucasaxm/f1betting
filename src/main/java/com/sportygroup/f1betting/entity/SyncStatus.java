package com.sportygroup.f1betting.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "sync_status", uniqueConstraints = {
    @jakarta.persistence.UniqueConstraint(name = "uq_sync_status_provider_year", columnNames = {"provider_id", "year"})
})
public class SyncStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "last_synced", nullable = false)
    private Instant lastSynced;

    public SyncStatus(UUID id, Provider provider, Integer year, Instant lastSynced) {
        this.id = id;
        this.provider = provider;
        this.year = year;
        this.lastSynced = lastSynced;
    }
}
