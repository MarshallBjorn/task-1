package com.navrotskyi.trippyapi.mapper;

import com.navrotskyi.trippyapi.domain.TripParticipant;
import com.navrotskyi.trippyapi.dto.TripParticipantDto;

public class TripParticipantMapper {

    public static TripParticipantDto toDto(TripParticipant participant) {
        TripParticipantDto dto = new TripParticipantDto();
        dto.setId(participant.getId());
        dto.setUserId(participant.getUser().getId());
        dto.setUserName(participant.getUser().getName());
        dto.setUserPhotoUrl(participant.getUser().getPhotoUrl());
        dto.setEventId(participant.getEvent().getId());
        dto.setTripRole(participant.getTripRole().getName());
        dto.setPersonalBudget(participant.getPersonalBudget());
        dto.setAccepted(participant.isAccepted());
        return dto;
    }
}