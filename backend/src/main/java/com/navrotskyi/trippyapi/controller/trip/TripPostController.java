package com.navrotskyi.trippyapi.controller.trip;

import com.navrotskyi.trippyapi.dto.admin.PostResponse;
import com.navrotskyi.trippyapi.dto.trip.PostCreateRequest;
import com.navrotskyi.trippyapi.dto.trip.PostUpdateRequest;
import com.navrotskyi.trippyapi.service.trip.TripPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@Tag(name = "User - Trip Posts", description = "Endpoints for managing trip posts and their photos")
@RequiredArgsConstructor
public class TripPostController {

    private final TripPostService postService;

    /**
     * Retrieves all posts associated with a specific trip node.
     * Useful for lazy loading or paginating posts independently of the node details.
     *
     * @param nodeId      UUID of the trip node
     * @param userDetails Authenticated user's details
     * @return List of posts with their photos
     */
    @Operation(summary = "Get all posts for a node", description = "Retrieves a list of posts attached to a specific node. User must be an accepted participant of the trip.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "User is not an accepted participant of this trip", content = @Content),
            @ApiResponse(responseCode = "404", description = "Node not found", content = @Content)
    })
    @GetMapping("/node/{nodeId}")
    public ResponseEntity<List<PostResponse>> getPostsByNode(
            @Parameter(description = "ID of the trip node") @PathVariable UUID nodeId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(postService.getPostsByNodeId(nodeId, userDetails.getUsername()));
    }

    /**
     * Creates a new post with optional photo attachments within a specific node.
     *
     * @param request     JSON payload containing node ID and post text
     * @param photos      Optional list of multipart files (photos)
     * @param userDetails Authenticated user's details
     * @return The created post with generated URLs for photos
     */
    @Operation(summary = "Create a new post", description = "Creates a post and uploads optional photos. Consumes multipart/form-data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Post created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or file size exceeded", content = @Content),
            @ApiResponse(responseCode = "403", description = "User is not an accepted participant", content = @Content)
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse> createPost(
            @Parameter(description = "Post data (JSON)") @RequestPart("data") @Valid PostCreateRequest request,
            @Parameter(description = "List of photo files") @RequestPart(value = "photos", required = false) List<MultipartFile> photos,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        PostResponse response = postService.createPostWithPhotos(request, photos, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates the text content of an existing post.
     * Only the author of the post can perform this action.
     *
     * @param postId      UUID of the post to update
     * @param request     JSON payload with new text
     * @param userDetails Authenticated user's details
     * @return The updated post
     */
    @Operation(summary = "Update an existing post", description = "Updates post content. Only the author can edit their post.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post updated successfully"),
            @ApiResponse(responseCode = "403", description = "User is not the author of this post", content = @Content),
            @ApiResponse(responseCode = "404", description = "Post not found", content = @Content)
    })
    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @Parameter(description = "ID of the post") @PathVariable UUID postId,
            @Valid @RequestBody PostUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(postService.updatePost(postId, request, userDetails.getUsername()));
    }

    /**
     * Deletes a post and all its associated photos.
     * Can be performed by the post author, or trip ORGANIZER/MODERATOR.
     *
     * @param postId      UUID of the post to delete
     * @param userDetails Authenticated user's details
     * @return No content
     */
    @Operation(summary = "Delete a post", description = "Deletes a post and its photos. Requires author, ORGANIZER, or MODERATOR role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Post deleted successfully"),
            @ApiResponse(responseCode = "403", description = "User lacks permission to delete this post", content = @Content),
            @ApiResponse(responseCode = "404", description = "Post not found", content = @Content)
    })
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "ID of the post") @PathVariable UUID postId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        postService.deletePost(postId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes a specific photo from a post.
     * Can be performed by the post author, or trip ORGANIZER/MODERATOR.
     *
     * @param photoId     UUID of the photo to delete
     * @param userDetails Authenticated user's details
     * @return No content
     */
    @Operation(summary = "Delete a photo", description = "Deletes a specific photo. Requires post author, ORGANIZER, or MODERATOR role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Photo deleted successfully"),
            @ApiResponse(responseCode = "403", description = "User lacks permission to delete this photo", content = @Content),
            @ApiResponse(responseCode = "404", description = "Photo not found", content = @Content)
    })
    @DeleteMapping("/photos/{photoId}")
    public ResponseEntity<Void> deletePhoto(
            @Parameter(description = "ID of the photo") @PathVariable UUID photoId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        postService.deletePhoto(photoId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}