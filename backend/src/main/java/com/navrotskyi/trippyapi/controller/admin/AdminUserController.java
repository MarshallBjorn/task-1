package com.navrotskyi.trippyapi.controller.admin;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.navrotskyi.trippyapi.dto.admin.UserAdminResponse;
import com.navrotskyi.trippyapi.dto.admin.UserCreateRequest;
import com.navrotskyi.trippyapi.dto.admin.UserUpdateRequest;
import com.navrotskyi.trippyapi.service.admin.AdminUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST Controller providing administrative operations on users.
 * Allows retrieving, creating, updating, and deleting accounts.
 * Access to these endpoints is restricted strictly to the ADMIN role.
 */
@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin - Users", description = "User account management for system administrators")
public class AdminUserController {
    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * Retrieves a list of all registered users.
     *
     * @return a list of DTO objects containing secure user data
     */
    @Operation(summary = "Get all users", description = "Returns a complete list of all user accounts in the system (excluding passwords).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of users"),
        @ApiResponse(responseCode = "403", description = "Missing administrator privileges")
    })
    @GetMapping
    public ResponseEntity<List<UserAdminResponse>> getAllUsers() {
        List<UserAdminResponse> users = adminUserService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Retrieves details of a specific user based on their unique ID.
     *
     * @param id the unique identifier of the user in the database
     * @return secure user data
     */
    @Operation(summary = "Get user by ID", description = "Returns details of a single user if they exist in the system.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully found the user"),
        @ApiResponse(responseCode = "403", description = "Missing administrator privileges"),
        @ApiResponse(responseCode = "404", description = "User with the specified ID not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserAdminResponse> getUserById(
        @Parameter(description = "User ID", required = true) @PathVariable UUID id
    ) {
        UserAdminResponse user = adminUserService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Creates a new user account from the administrative panel.
     *
     * @param request validated data required to create an account (e.g., strong password and role)
     * @return the created user and a Location header with the URI to the new resource
     */
    @Operation(summary = "Create a new user", description = "Creates a brand new account. The endpoint verifies email availability and password strength.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully created the user account"),
        @ApiResponse(responseCode = "400", description = "Input validation error (e.g., email already taken, password too weak)"),
        @ApiResponse(responseCode = "403", description = "Missing administrator privileges")
    })
    @PostMapping
    public ResponseEntity<UserAdminResponse> createUser(
        @Valid @RequestBody UserCreateRequest request
    ) {
        UserAdminResponse createdUser = adminUserService.createUser(request);

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(createdUser.id())
            .toUri();

        return ResponseEntity.created(location).body(createdUser);
    }

    /**
     * Updates parameters of an existing account (e.g., applying a ban, changing role).
     *
     * @param id the identifier of the user being updated
     * @param request validated update data
     * @return the user object after applying changes
     */
    @Operation(summary = "Update user", description = "Modifies the name, role, or applies a ban (isBlocked) to a user account.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated user data"),
        @ApiResponse(responseCode = "400", description = "Data validation error (e.g., empty name)"),
        @ApiResponse(responseCode = "403", description = "Missing administrator privileges"),
        @ApiResponse(responseCode = "404", description = "User with the specified ID not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserAdminResponse> updateUser(
        @Parameter(description = "User ID", required = true) @PathVariable UUID id,
        @Valid @RequestBody UserUpdateRequest request
    ) {
        UserAdminResponse updatedUser = adminUserService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Irreversibly deletes a user account from the database (hard delete).
     *
     * @param id the identifier of the user to be deleted
     * @return an empty 204 No Content status
     */
    @Operation(summary = "Delete user", description = "Performs a physical deletion of the user record from the database (hard delete).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User successfully deleted"),
        @ApiResponse(responseCode = "403", description = "Missing administrator privileges"),
        @ApiResponse(responseCode = "404", description = "User with the specified ID not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
        @Parameter(description = "User ID", required = true) @PathVariable UUID id
    ) {
        adminUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Uploads or replaces a profile photo for a specific user via admin panel.
     *
     * @param id the identifier of the user
     * @param file the image file to upload
     * @return the updated user object
     */
    @Operation(summary = "Upload user photo", description = "Uploads or replaces the profile photo for a specific user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully uploaded the photo"),
        @ApiResponse(responseCode = "400", description = "Invalid file or size exceeded"),
        @ApiResponse(responseCode = "403", description = "Missing administrator privileges"),
        @ApiResponse(responseCode = "404", description = "User with the specified ID not found")
    })
    @PostMapping("/{id}/photo")
    public ResponseEntity<UserAdminResponse> uploadUserPhoto(
        @Parameter(description = "User ID", required = true) @PathVariable UUID id,
        @RequestParam("file") MultipartFile file
    ) {
        UserAdminResponse updatedUser = adminUserService.uploadUserPhoto(id, file);
        return ResponseEntity.ok(updatedUser);
    }
}
