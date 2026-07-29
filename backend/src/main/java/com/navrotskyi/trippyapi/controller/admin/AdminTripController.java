package com.navrotskyi.trippyapi.controller.admin;

import com.navrotskyi.trippyapi.dto.admin.TripDetailsResponse;
import com.navrotskyi.trippyapi.dto.admin.TripResponse;
import com.navrotskyi.trippyapi.service.admin.AdminTripService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controller for viewing all trip events by an administrator.
 * Provides a read-only overview of the platform's activity.
 */
@RestController
@RequestMapping("/api/admin/trips")
@Tag(name = "Admin Trips", description = "Trip monitoring (Read-Only) - ADMIN access only")
public class AdminTripController {

    private final AdminTripService tripService;

    public AdminTripController(AdminTripService tripService) {
        this.tripService = tripService;
    }

    /**
     * Retrieves a list of all trips created in the application.
     *
     * @return list of trips with basic metadata
     */
    @Operation(
            summary = "Get all trips",
            description = "Returns a high-level overview of all trips in the system. Does not include detailed nodes or participant lists."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of trips retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized (JWT token required)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden (ADMIN role required)", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<TripResponse>> getAllTrips() {
        return ResponseEntity.ok(tripService.getAllTrips());
    }

    /**
     * Retrieves details of a specific trip
     *
     * @return list of trips details and nodes
     */
    @Operation(
            summary = "Get trip details",
            description = "Returns a high-level overview of trip details"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trip details retrieved succesfuly."),
            @ApiResponse(responseCode = "401", description = "Unauthorized (JWT token required)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden (ADMIN role required)", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<TripDetailsResponse> getTripDetails(
        @Parameter(description = "Trip ID", required = true) @PathVariable("id") UUID tripId
    ) {
        TripDetailsResponse tripDetails = tripService.getTripDetails(tripId);
        return ResponseEntity.ok(tripDetails);
    } 
}