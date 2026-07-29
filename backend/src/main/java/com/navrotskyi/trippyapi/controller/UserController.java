package com.navrotskyi.trippyapi.controller;

import com.navrotskyi.trippyapi.dto.UpdateUserRequest;
import com.navrotskyi.trippyapi.dto.UserResponse;
import com.navrotskyi.trippyapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import com.navrotskyi.trippyapi.dto.user.InvitationDto;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Endpoints for managing user accounts")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user", description = "Retrieves the details of the currently authenticated user context.")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        UserResponse response = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user", description = "Updates details (name, email, password) for the currently authenticated user.")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateCurrentUser(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Uploads or replaces a profile photo for a specific user via admin panel.
     *
     * @param id   the identifier of the user
     * @param file the image file to upload
     * @return the updated user object
     */
    @PostMapping("/me/photo")
    @Operation(summary = "Upload profile photo", description = "Uploads a new profile picture for the currently authenticated user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully uploaded the photo"),
        @ApiResponse(responseCode = "400", description = "Invalid file or size exceeded"),
        @ApiResponse(responseCode = "403", description = "Missing privileges"),
        @ApiResponse(responseCode = "404", description = "User with the specified ID not found")
    })
    public ResponseEntity<UserResponse> uploadProfilePhoto(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        UserResponse response = userService.uploadProfilePhoto(userDetails.getUsername(), file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/invitations")
    @Operation(summary = "Get my invitations", description = "Returns pending trip invitations for the current user.")
    public ResponseEntity<List<InvitationDto>> getMyInvitations(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<InvitationDto> invitations = userService.getInvitations(userDetails.getUsername());
        return ResponseEntity.ok(invitations);
    }
}

/*
 ** How it works end to end:**
 * ```
 * PUT /api/users/me
 * Authorization: Bearer <jwt>
 * Content-Type: application/json
 * 
 * {
 * "name": "New Name", // optional
 * "email": "new@email.com", // optional — checked for uniqueness
 * "password": "newSecret123" // optional — re-hashed before saving
 * }
 * 
 * → 200 OK { id, name, email, role, isVerified }
 * → 400 if email is already taken by another account
 * → 403 if no/invalid JWT
 */