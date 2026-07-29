package com.navrotskyi.trippyapi.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "trip_participant")
public class TripParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private TripEvent event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_role_id", nullable = false)
    private TripRole tripRole;

    @Column(name = "personal_budget", nullable = false, precision = 12, scale = 2)
    private BigDecimal personalBudget = BigDecimal.ZERO;

    @Column(name = "is_accepted", nullable = false)
    private boolean isAccepted = false;

    public TripParticipant() {}

    public TripParticipant(
        TripEvent event,
        User user,
        TripRole tripRole,
        BigDecimal personalBudget,
        boolean isAccepted
    ) {
        this.event = event;
        this.user = user;
        this.tripRole = tripRole;
        this.personalBudget = personalBudget;
        this.isAccepted = isAccepted;
    }

    // --- GETTERY I SETTERY ---
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public TripEvent getEvent() { return event; }
    public void setEvent(TripEvent event) { this.event = event; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public TripRole getTripRole() { return tripRole; }
    public void setTripRole(TripRole tripRole) { this.tripRole = tripRole; }

    public BigDecimal getPersonalBudget() { return personalBudget; }
    public void setPersonalBudget(BigDecimal personalBudget) { this.personalBudget = personalBudget; }

    public boolean isAccepted() { return isAccepted; }
    public void setAccepted(boolean accepted) { isAccepted = accepted; }
}