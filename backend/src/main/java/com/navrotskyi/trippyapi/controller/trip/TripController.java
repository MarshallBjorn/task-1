package com.navrotskyi.trippyapi.controller.trip;

import com.navrotskyi.trippyapi.dto.trip.TripInviteRequest;
import com.navrotskyi.trippyapi.service.TripService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.security.Principal;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trips")
@Tag(name = "Trips", description = "Endpoints for managing user trips and participants")
public class TripController {
    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    /**
     * Invites a user to a specific trip by their email.
     * Only the trip organizer can perform this action.
     *
     * @param id        trip ID
     * @param request   object containing the invited user's email
     * @param principal currently authenticated user
     */
    @Operation(summary = "Invite user to a trip", description = "Sends an invitation to a registered user via their email. Creates a new record in trip participants with 'PARTICIPANT' role and accepted=false. Only the trip organizer can invoke this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invitation sent successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or user is already a participant", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized (JWT token required)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden (User is not the organizer of this trip)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trip or User with provided email not found", content = @Content)
    })
    @PostMapping("/{id}/invite")
    public ResponseEntity<Void> inviteUserToTrip(
            @PathVariable UUID id,
            @Valid @RequestBody TripInviteRequest request,
            Principal principal) {

        String currentUsername = principal.getName();

        tripService.inviteUserToTrip(id, request, currentUsername);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/accept")
    @Operation(summary = "Accept trip invitation")
    public ResponseEntity<Void> acceptInvitation(
            @PathVariable UUID id,
            Principal principal) {

        String email = principal.getName();
        tripService.acceptInvitation(id, email);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject trip invitation")
    public ResponseEntity<Void> rejectInvitation(
            @PathVariable UUID id,
            Principal principal) {

        String email = principal.getName();
        tripService.rejectInvitation(id, email);

        return ResponseEntity.ok().build();
    }
}
