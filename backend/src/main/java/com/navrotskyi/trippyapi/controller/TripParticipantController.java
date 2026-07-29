package com.navrotskyi.trippyapi.controller;

import com.navrotskyi.trippyapi.domain.TripParticipant;
import com.navrotskyi.trippyapi.domain.User;
import com.navrotskyi.trippyapi.dto.InviteParticipantRequest;
import com.navrotskyi.trippyapi.dto.TripParticipantDto;
import com.navrotskyi.trippyapi.mapper.TripParticipantMapper;
import com.navrotskyi.trippyapi.service.TripParticipantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trips/{eventId}/participants")
@Tag(name = "Trip Participants", description = "Endpoints for managing trip participants")
public class TripParticipantController {

    private final TripParticipantService tripParticipantService;

    public TripParticipantController(TripParticipantService tripParticipantService) {
        this.tripParticipantService = tripParticipantService;
    }

    @PostMapping
    @Operation(summary = "Invite a participant", description = "Invites a user to a trip event using their email address.")
    public ResponseEntity<TripParticipantDto> inviteParticipant(@PathVariable UUID eventId, @RequestBody InviteParticipantRequest request, @AuthenticationPrincipal User currentUser) {
        TripParticipant newParticipant = tripParticipantService.inviteParticipant(eventId, request, currentUser);
        return new ResponseEntity<>(TripParticipantMapper.toDto(newParticipant), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get trip participants", description = "Retrieves a list of all participants for a specific trip event. Only accessible to active participants of the trip.")
    public ResponseEntity<List<TripParticipantDto>> getTripParticipants(
            @PathVariable UUID eventId,
            @AuthenticationPrincipal User currentUser
    ) {
        List<TripParticipant> participants = tripParticipantService.getParticipantsForEvent(eventId, currentUser);
        List<TripParticipantDto> participantDtos = participants.stream().map(TripParticipantMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(participantDtos);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Remove a participant",
            description = "Removes a participant from the trip. Only the trip owner can perform this action. The owner cannot be removed.")
    public ResponseEntity<Void> removeParticipant(
            @PathVariable UUID eventId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal User currentUser
    ) {
        tripParticipantService.removeTripParticipant(eventId, userId, currentUser);
        return ResponseEntity.noContent().build();
    }
}