package com.navrotskyi.trippyapi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class TripEventDto {
    private UUID id;
    private String name;
    private UUID ownerId;
    private String currencyCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal budget;
    private List<TripParticipantDto> participants;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }
    public List<TripParticipantDto> getParticipants() { return participants; }
    public void setParticipants(List<TripParticipantDto> participants) { this.participants = participants; }
}