package com.sportygroup.f1betting.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "drivers")
public class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "full_name", nullable = false, length = Integer.MAX_VALUE)
    private String fullName;

    @OneToMany(mappedBy = "driver")
    private Set<DriverExternalRef> driverExternalRefs = new LinkedHashSet<>();

    @OneToMany(mappedBy = "driver")
    private Set<EventOdd> eventOdds = new LinkedHashSet<>();

}
