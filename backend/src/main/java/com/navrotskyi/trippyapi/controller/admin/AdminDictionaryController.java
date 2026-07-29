package com.navrotskyi.trippyapi.controller.admin;

import com.navrotskyi.trippyapi.dto.admin.CurrencyRequest;
import com.navrotskyi.trippyapi.dto.admin.CurrencyResponse;
import com.navrotskyi.trippyapi.dto.admin.TripRoleRequest;
import com.navrotskyi.trippyapi.dto.admin.TripRoleResponse;
import com.navrotskyi.trippyapi.service.admin.AdminDictionaryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing system dictionaries (currencies, trip roles) by an administrator.
 * Provides endpoints necessary for the initial application configuration before use.
 */
@RestController
@RequestMapping("/api/admin/dictionaries")
@Tag(name = "Admin Dictionaries", description = "System dictionary management (Currencies, Roles) - ADMIN access only")
public class AdminDictionaryController {

    private final AdminDictionaryService dictionaryService;

    public AdminDictionaryController(AdminDictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Adds a new currency to the system.
     *
     * @param request object containing a 3-letter code and the full name of the currency
     * @return saved currency (CurrencyResponse)
     */
    @Operation(
            summary = "Add a new currency",
            description = "Creates a new currency (e.g., USD, PLN) that users can select when planning their trips."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Currency added successfully",
                    content = @Content(schema = @Schema(implementation = CurrencyResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g., code too long) or currency already exists", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized (JWT token required)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden (ADMIN role required)", content = @Content)
    })
    @PostMapping("/currencies")
    public ResponseEntity<CurrencyResponse> addCurrency(@Valid @RequestBody CurrencyRequest request) {
        return new ResponseEntity<>(dictionaryService.addCurrency(request), HttpStatus.CREATED);
    }

    /**
     * Retrieves a list of all available currencies in the system.
     *
     * @return list of currencies
     */
    @Operation(
            summary = "Get all currencies",
            description = "Returns a list of all defined currencies in the system."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of currencies retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized (JWT token required)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden (ADMIN role required)", content = @Content)
    })
    @GetMapping("/currencies")
    public ResponseEntity<List<CurrencyResponse>> getAllCurrencies() {
        return ResponseEntity.ok(dictionaryService.getAllCurrencies());
    }

    /**
     * Adds a new trip role to the system.
     *
     * @param request object containing the role name (e.g., ORGANIZER, VIEWER)
     * @return saved role (TripRoleResponse)
     */
    @Operation(
            summary = "Add a new trip role",
            description = "Creates a new system role that can be assigned to trip participants (e.g., manager, observer)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Role added successfully",
                    content = @Content(schema = @Schema(implementation = TripRoleResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or role with the given name already exists", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized (JWT token required)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden (ADMIN role required)", content = @Content)
    })
    @PostMapping("/roles")
    public ResponseEntity<TripRoleResponse> addTripRole(@Valid @RequestBody TripRoleRequest request) {
        return new ResponseEntity<>(dictionaryService.addTripRole(request), HttpStatus.CREATED);
    }

    /**
     * Retrieves a list of all available trip roles.
     *
     * @return list of roles
     */
    @Operation(
            summary = "Get all trip roles",
            description = "Returns a list of roles that can be assigned to participants during a trip."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of roles retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized (JWT token required)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden (ADMIN role required)", content = @Content)
    })
    @GetMapping("/roles")
    public ResponseEntity<List<TripRoleResponse>> getAllTripRoles() {
        return ResponseEntity.ok(dictionaryService.getAllTripRoles());
    }
}