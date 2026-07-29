package com.navrotskyi.trippyapi.controller.admin;

import com.navrotskyi.trippyapi.dto.admin.AdminPhotoResponse;
import com.navrotskyi.trippyapi.dto.admin.AdminPostResponse;
import com.navrotskyi.trippyapi.service.admin.AdminModerationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for content moderation by an administrator.
 * Allows viewing and deleting user-generated content (posts, photos) that violates the rules.
 */
@RestController
@RequestMapping("/api/admin/moderation")
@Tag(name = "Admin Moderation", description = "Content moderation (Posts, Photos) - ADMIN access only")
public class AdminModerationController {

    private final AdminModerationService moderationService;

    public AdminModerationController(AdminModerationService moderationService) {
        this.moderationService = moderationService;
    }

    // ==========================================
    // POSTS
    // ==========================================

    /**
     * Retrieves a list of all posts created by users.
     *
     * @return list of posts
     */
    @Operation(summary = "Get all posts", description = "Returns a list of all posts to review for inappropriate content.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of posts retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized (JWT token required)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden (ADMIN role required)", content = @Content)
    })
    @GetMapping("/posts")
    public ResponseEntity<List<AdminPostResponse>> getAllPosts() {
        return ResponseEntity.ok(moderationService.getAllPosts());
    }

    /**
     * Deletes a specific post by its ID.
     *
     * @param id the ID of the post to delete
     */
    @Operation(summary = "Delete a post", description = "Permanently deletes a post and cascades the deletion to its associated photos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Post deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Post not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized (JWT token required)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden (ADMIN role required)", content = @Content)
    })
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID id) {
        moderationService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // PHOTOS
    // ==========================================

    /**
     * Retrieves a list of all photos uploaded by users.
     *
     * @return list of photos
     */
    @Operation(summary = "Get all photos", description = "Returns a list of all uploaded photos for moderation purposes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of photos retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @GetMapping("/photos")
    public ResponseEntity<List<AdminPhotoResponse>> getAllPhotos() {
        return ResponseEntity.ok(moderationService.getAllPhotos());
    }

    /**
     * Deletes a specific photo by its ID.
     *
     * @param id the ID of the photo to delete
     */
    @Operation(summary = "Delete a photo", description = "Permanently removes a specific photo if it violates terms of service.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Photo deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Photo not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @DeleteMapping("/photos/{id}")
    public ResponseEntity<Void> deletePhoto(@PathVariable UUID id) {
        moderationService.deletePhoto(id);
        return ResponseEntity.noContent().build();
    }
}