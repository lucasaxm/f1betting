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
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

import com.sportygroup.f1betting.entity.ProviderName;

@Getter
@Setter
@Entity
@Table(name = "driver_external_ref", uniqueConstraints = {
    @UniqueConstraint(name = "uq_driver_ext_ref", columnNames = {"provider_name", "external_id"})
})
public class DriverExternalRef {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "provider_name", nullable = false, length = 50)
    private ProviderName providerName;

    @Size(max = 100)
    @NotNull
    @Column(name = "external_id", nullable = false, length = 100)
    private String externalId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

}
