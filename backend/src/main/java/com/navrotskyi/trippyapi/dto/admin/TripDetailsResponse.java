package com.navrotskyi.trippyapi.dto.admin;

import java.util.List;
import java.util.UUID;

public record TripDetailsResponse(
    UUID id, 
    String name, 
    String ownerName, 
    java.time.LocalDate startDate, 
    java.time.LocalDate endDate, 
    java.math.BigDecimal budget,
    List<NodeResponse> nodes
) {}