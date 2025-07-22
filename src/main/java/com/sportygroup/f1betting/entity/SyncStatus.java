package com.sportygroup.f1betting.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "sync_status")
public class SyncStatus {
    @Id
    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "last_synced", nullable = false)
    private Instant lastSynced;

    public SyncStatus(Integer year, Instant lastSynced) {
        this.year = year;
        this.lastSynced = lastSynced;
    }
}
