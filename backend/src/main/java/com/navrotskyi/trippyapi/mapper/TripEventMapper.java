package com.navrotskyi.trippyapi.mapper;

import com.navrotskyi.trippyapi.domain.TripEvent;
import com.navrotskyi.trippyapi.dto.TripEventDto;

import java.util.stream.Collectors;

public class TripEventMapper {

    public static TripEventDto toDto(TripEvent event) {
        TripEventDto dto = new TripEventDto();
        dto.setId(event.getId());
        dto.setName(event.getName());
        dto.setOwnerId(event.getOwner().getId());
        dto.setCurrencyCode(event.getCurrency().getCode());
        dto.setStartDate(event.getStartDate());
        dto.setEndDate(event.getEndDate());
        dto.setBudget(event.getBudget());
        if (event.getParticipants() != null) {
            dto.setParticipants(event.getParticipants().stream()
                .map(TripParticipantMapper::toDto)
                .collect(Collectors.toList()));
        }
        return dto;
    }
}