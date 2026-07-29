package com.navrotskyi.trippyapi.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "trip_role")
public class TripRole {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 50)
    private String name;

    public TripRole() {}

    public TripRole(String name) {
        this.name = name;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}