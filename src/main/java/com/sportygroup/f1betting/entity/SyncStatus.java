package com.sportygroup.f1betting.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "sync_status")
public class SyncStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "provider_name", nullable = false, length = 50)
    private String providerName;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "last_synced", nullable = false)
    private Instant lastSynced;

    public SyncStatus(UUID id, String providerName, Integer year, Instant lastSynced) {
        this.id = id;
        this.providerName = providerName;
        this.year = year;
        this.lastSynced = lastSynced;
    }
}
