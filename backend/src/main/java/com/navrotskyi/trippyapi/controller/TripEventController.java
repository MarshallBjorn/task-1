package com.navrotskyi.trippyapi.controller;

import com.navrotskyi.trippyapi.domain.TripEvent;
import com.navrotskyi.trippyapi.domain.User;
import com.navrotskyi.trippyapi.dto.TripEventDto;
import com.navrotskyi.trippyapi.dto.trip.CreateTripEventRequest;
import com.navrotskyi.trippyapi.mapper.TripEventMapper;
import com.navrotskyi.trippyapi.service.TripEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trips")
@Tag(name = "Trip Events", description = "Endpoints for managing trip events")
public class TripEventController {

    private final TripEventService tripEventService;

    public TripEventController(TripEventService tripEventService) {
        this.tripEventService = tripEventService;
    }

    @PostMapping
    @Operation(summary = "Create a new trip event", description = "Creates a new trip and sets the currently authenticated user as the organizer.")
    public ResponseEntity<TripEventDto> createTripEvent(@Valid @RequestBody CreateTripEventRequest request, @AuthenticationPrincipal User currentUser) {
        TripEvent newTripEvent = tripEventService.createTripEvent(request, currentUser);
        return new ResponseEntity<>(TripEventMapper.toDto(newTripEvent), HttpStatus.CREATED);
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "Get trip event by ID", description = "Retrieves details of a specific trip event, including its participants, by its UUID.")
    public ResponseEntity<TripEventDto> getTripEventById(
        @PathVariable UUID eventId,
        @AuthenticationPrincipal User currentUser
    ) {
        TripEvent tripEvent = tripEventService.getTripEventById(eventId, currentUser.getId());
        return ResponseEntity.ok(TripEventMapper.toDto(tripEvent));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my trip events", description = "Retrieves a list of trip events created by the currently authenticated user.")
    public ResponseEntity<List<TripEventDto>> getMyTripEvents(@AuthenticationPrincipal User currentUser) {
        List<TripEvent> trips = tripEventService.getUserTrips(currentUser);
        List<TripEventDto> tripDtos = trips.stream().map(TripEventMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(tripDtos);
    }
}