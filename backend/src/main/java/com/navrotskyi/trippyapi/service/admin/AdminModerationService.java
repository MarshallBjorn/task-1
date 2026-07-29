package com.navrotskyi.trippyapi.service.admin;

import com.navrotskyi.trippyapi.dto.admin.AdminPhotoResponse;
import com.navrotskyi.trippyapi.dto.admin.AdminPostResponse;
import com.navrotskyi.trippyapi.repository.TripPhotoRepository;
import com.navrotskyi.trippyapi.repository.TripPostRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminModerationService {

    private final TripPostRepository postRepository;
    private final TripPhotoRepository photoRepository;

    public AdminModerationService(TripPostRepository postRepository, TripPhotoRepository photoRepository) {
        this.postRepository = postRepository;
        this.photoRepository = photoRepository;
    }

    // --- POSTS MODERATION ---
    @Transactional(readOnly = true)
    public List<AdminPostResponse> getAllPosts() {
        return postRepository.findAll().stream()
                .map(post -> new AdminPostResponse(
                        post.getId(),
                        post.getNode().getId(),
                        post.getReporter().getName(),
                        post.getNote()
                ))
                .collect(Collectors.toList());
    }

    public void deletePost(UUID id) {
        if (!postRepository.existsById(id)) {
            throw new EntityNotFoundException("Post with ID " + id + " not found");
        }
        postRepository.deleteById(id);
    }

    // --- PHOTOS MODERATION ---
    @Transactional(readOnly = true)
    public List<AdminPhotoResponse> getAllPhotos() {
        return photoRepository.findAll().stream()
                .map(photo -> new AdminPhotoResponse(
                        photo.getId(),
                        photo.getPost().getId(),
                        photo.getPost().getReporter().getName(), // Wyciągamy autora posta
                        photo.getPhotoUrl()
                ))
                .collect(Collectors.toList());
    }

    public void deletePhoto(UUID id) {
        if (!photoRepository.existsById(id)) {
            throw new EntityNotFoundException("Photo with ID " + id + " not found");
        }
        photoRepository.deleteById(id);
    }
}