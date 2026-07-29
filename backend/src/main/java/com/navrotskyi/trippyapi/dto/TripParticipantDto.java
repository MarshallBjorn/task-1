package com.navrotskyi.trippyapi.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class TripParticipantDto {
    private UUID id;
    private UUID userId;
    private String userName;
    private String userPhotoUrl;
    private UUID eventId;
    private String tripRole;
    private BigDecimal personalBudget;
    private boolean isAccepted;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserPhotoUrl() { return userPhotoUrl; }
    public void setUserPhotoUrl(String userPhotoUrl) { this.userPhotoUrl = userPhotoUrl; }
    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }
    public String getTripRole() { return tripRole; }
    public void setTripRole(String tripRole) { this.tripRole = tripRole; }
    public BigDecimal getPersonalBudget() { return personalBudget; }
    public void setPersonalBudget(BigDecimal personalBudget) { this.personalBudget = personalBudget; }
    public boolean isAccepted() { return isAccepted; }
    public void setAccepted(boolean accepted) { isAccepted = accepted; }
}