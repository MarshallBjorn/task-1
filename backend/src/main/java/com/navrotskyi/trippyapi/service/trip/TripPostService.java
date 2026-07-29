package com.navrotskyi.trippyapi.service.trip;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.AccessDeniedException;

import com.navrotskyi.trippyapi.domain.TripEvent;
import com.navrotskyi.trippyapi.domain.TripNode;
import com.navrotskyi.trippyapi.domain.TripParticipant;
import com.navrotskyi.trippyapi.domain.TripPhoto;
import com.navrotskyi.trippyapi.domain.TripPost;
import com.navrotskyi.trippyapi.domain.User;
import com.navrotskyi.trippyapi.dto.admin.PhotoResponse;
import com.navrotskyi.trippyapi.dto.admin.PostResponse;
import com.navrotskyi.trippyapi.dto.trip.PostCreateRequest;
import com.navrotskyi.trippyapi.dto.trip.PostUpdateRequest;
import com.navrotskyi.trippyapi.repository.TripNodeRepository;
import com.navrotskyi.trippyapi.repository.TripParticipantRepository;
import com.navrotskyi.trippyapi.repository.TripPhotoRepository;
import com.navrotskyi.trippyapi.repository.TripPostRepository;
import com.navrotskyi.trippyapi.repository.UserRepository;
import com.navrotskyi.trippyapi.service.FileStorageService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripPostService {
    private final TripPostRepository tripPostRepository;
    private final TripNodeRepository tripNodeRepository;
    private final TripParticipantRepository tripParticipantRepository;
    private final UserRepository userRepository;
    private final TripPhotoRepository tripPhotoRepository;
    private final FileStorageService fileStorageService;

    @Value("${app.file-server.url:http://localhost:8888/uploads/}")
    private String fileServerBaseUrl;

    @Transactional(readOnly = true)
    public List<PostResponse> getPostsByNodeId(UUID nodeId, String userEmail) {
        User user = getUserByEmail(userEmail);
        TripNode node = tripNodeRepository.findById(nodeId)
                .orElseThrow(() -> new EntityNotFoundException("Węzeł nie istnieje"));

        validateUserIsActiveParticipant(user, node.getEvent());

        return node.getPosts().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public PostResponse createPostWithPhotos(PostCreateRequest request, List<MultipartFile> files, String userEmail) {
        User reporter = getUserByEmail(userEmail);
        
        TripNode node = tripNodeRepository.findById(request.nodeId())
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono węzła wycieczki"));

        validateUserIsActiveParticipant(reporter, node.getEvent());

        TripPost post = new TripPost();
        post.setNode(node);
        post.setReporter(reporter);
        post.setNote(request.note());

        if (files != null && !files.isEmpty()) {
            files.forEach(file -> addPhotoToPost(post, file));
        }

        TripPost savedPost = tripPostRepository.save(post);
        return mapToResponse(savedPost);
    }

    @Transactional
    public PostResponse updatePost(UUID postId, PostUpdateRequest request, String userEmail) {
        TripPost post = getPostById(postId);
        
        if (!post.getReporter().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("Nie masz uprawnień do edycji tego posta. Tylko autor może zmieniać treść.");
        }

        post.setNote(request.note());
        return mapToResponse(tripPostRepository.save(post));
    }

    @Transactional
    public void deletePost(UUID postId, String userEmail) {
        TripPost post = getPostById(postId);
        User user = getUserByEmail(userEmail);
        TripEvent event = post.getNode().getEvent();

        boolean isAuthor = post.getReporter().getEmail().equals(userEmail);      
        boolean hasManagementPower = isUserHasRole(user, event, List.of("ORGANIZER", "MODERATOR"));

        if (!isAuthor && !hasManagementPower) {
            throw new AccessDeniedException("Nie masz uprawnień do usunięcia tego posta.");
        }

        post.getPhotos().forEach(photo -> fileStorageService.deletePhoto(photo.getPhotoUrl()));
        tripPostRepository.delete(post);
    }

    @Transactional
    public void deletePhoto(UUID photoId, String userEmail) {
        TripPhoto tripPhoto = tripPhotoRepository.findById(photoId)
                .orElseThrow(() -> new EntityNotFoundException("Zdjęcie nie istnieje"));

        TripPost post = tripPhoto.getPost();
        User user = getUserByEmail(userEmail);
        TripEvent event = post.getNode().getEvent();

        boolean isAuthor = post.getReporter().getEmail().equals(userEmail);
        boolean hasManagementPower = isUserHasRole(user, event, List.of("ORGANIZER", "MODERATOR"));

        if (!isAuthor && !hasManagementPower) {
            throw new AccessDeniedException("Nie masz uprawnień do usunięcia tego zdjęcia.");
        }

        String filenameToDelete = tripPhoto.getPhotoUrl();

        tripPhotoRepository.delete(tripPhoto);
        fileStorageService.deletePhoto(filenameToDelete);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono użytkownika"));
    }

    private TripPost getPostById(UUID id) {
        return tripPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post nie istnieje"));
    }

    private void validateUserIsActiveParticipant(User user, TripEvent event) {
        TripParticipant participant = tripParticipantRepository.findByEventIdAndUserId(event.getId(), user.getId())
                .orElseThrow(() -> new AccessDeniedException("Nie jesteś uczestnikiem tej wycieczki."));

        if (!participant.isAccepted()) {
            throw new AccessDeniedException("Musisz najpierw zaakceptować zaproszenie do wycieczki, aby móc dodawać posty.");
        }
    }

    private boolean isUserHasRole(User user, TripEvent event, List<String> allowedRoles) {
        return tripParticipantRepository.findByEventIdAndUserId(event.getId(), user.getId())
                .map(participant -> {
                    String roleName = participant.getTripRole().getName().toUpperCase();
                    return allowedRoles.contains(roleName);
                })
                .orElse(false);
    }

    private void addPhotoToPost(TripPost post, MultipartFile file) {
        String url = fileStorageService.savePhoto(file);
        TripPhoto photo = new TripPhoto();
        photo.setPhotoUrl(url);
        photo.setPost(post);
        post.getPhotos().add(photo);
    }

    private PostResponse mapToResponse(TripPost post) {
        List<PhotoResponse> photoResponses = post.getPhotos().stream()
                .map(p -> {
                    String fullPhotoUrl = (p.getPhotoUrl() != null) ? fileServerBaseUrl + p.getPhotoUrl() : null;
                    return new PhotoResponse(p.getId(), fullPhotoUrl);
                })
                .toList();

        return new PostResponse(
                post.getId(),
                post.getNote(),
                post.getReporter().getName(),
                photoResponses
        );
    }
}
