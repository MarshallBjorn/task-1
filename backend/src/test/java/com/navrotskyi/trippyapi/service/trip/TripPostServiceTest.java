package com.navrotskyi.trippyapi.service.trip;

import com.navrotskyi.trippyapi.domain.*;
import com.navrotskyi.trippyapi.dto.admin.PostResponse;
import com.navrotskyi.trippyapi.dto.trip.PostCreateRequest;
import com.navrotskyi.trippyapi.repository.TripNodeRepository;
import com.navrotskyi.trippyapi.repository.TripParticipantRepository;
import com.navrotskyi.trippyapi.repository.TripPostRepository;
import com.navrotskyi.trippyapi.repository.UserRepository;
import com.navrotskyi.trippyapi.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripPostServiceTest {

    @Mock private TripPostRepository tripPostRepository;
    @Mock private TripNodeRepository tripNodeRepository;
    @Mock private TripParticipantRepository tripParticipantRepository;
    @Mock private UserRepository userRepository;
    @Mock private FileStorageService fileStorageService;

    @InjectMocks
    private TripPostService tripPostService;

    private User author;
    private User randomUser;
    private TripEvent event;
    private TripNode node;
    private TripPost post;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tripPostService, "fileServerBaseUrl", "http://localhost:8888/uploads/");

        author = new User();
        author.setId(UUID.randomUUID());
        author.setEmail("author@test.com");

        randomUser = new User();
        randomUser.setId(UUID.randomUUID());
        randomUser.setEmail("random@test.com");

        event = new TripEvent();
        event.setId(UUID.randomUUID());

        node = new TripNode();
        node.setId(UUID.randomUUID());
        node.setEvent(event);

        post = new TripPost();
        post.setId(UUID.randomUUID());
        post.setReporter(author);
        post.setNode(node);
    }

    @Test
    void deletePost_ShouldThrowAccessDenied_WhenUserIsNotAuthorAndNotAdmin() {
        UUID postId = post.getId();
        String randomUserEmail = randomUser.getEmail();

        when(tripPostRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userRepository.findByEmail(randomUserEmail)).thenReturn(Optional.of(randomUser));
        
        TripRole participantRole = new TripRole("PARTICIPANT");
        TripParticipant participant = new TripParticipant();
        participant.setTripRole(participantRole);
        when(tripParticipantRepository.findByEventIdAndUserId(event.getId(), randomUser.getId()))
                .thenReturn(Optional.of(participant));

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class, 
                () -> tripPostService.deletePost(postId, randomUserEmail)
        );

        assertEquals("Nie masz uprawnień do usunięcia tego posta.", exception.getMessage());

        verify(tripPostRepository, never()).delete(any(TripPost.class));
    }

    @Test
    void deletePost_ShouldDelete_WhenUserIsAuthor() {
        UUID postId = post.getId();
        String authorEmail = author.getEmail();

        when(tripPostRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userRepository.findByEmail(authorEmail)).thenReturn(Optional.of(author));

        tripPostService.deletePost(postId, authorEmail);

        verify(tripPostRepository, times(1)).delete(post);
    }

    @Test
    void deletePost_ShouldDelete_WhenUserIsOrganizerButNotAuthor() {
        UUID postId = post.getId();
        String adminEmail = randomUser.getEmail();

        when(tripPostRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(randomUser));

        TripRole adminRole = new TripRole("ORGANIZER");
        TripParticipant adminParticipant = new TripParticipant();
        adminParticipant.setTripRole(adminRole);
        when(tripParticipantRepository.findByEventIdAndUserId(event.getId(), randomUser.getId()))
                .thenReturn(Optional.of(adminParticipant));

        tripPostService.deletePost(postId, adminEmail);

        verify(tripPostRepository, times(1)).delete(post);
    }

    @Test
    void createPostWithPhotos_ShouldThrowException_WhenUserNotAccepted() {
        PostCreateRequest request = new PostCreateRequest(node.getId(), "Testowy post");
        
        when(userRepository.findByEmail(author.getEmail())).thenReturn(Optional.of(author));
        when(tripNodeRepository.findById(node.getId())).thenReturn(Optional.of(node));

        TripParticipant unacceptedParticipant = new TripParticipant();
        unacceptedParticipant.setAccepted(false);
        when(tripParticipantRepository.findByEventIdAndUserId(event.getId(), author.getId()))
                .thenReturn(Optional.of(unacceptedParticipant));

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> tripPostService.createPostWithPhotos(request, null, author.getEmail())
        );

        assertEquals("Musisz najpierw zaakceptować zaproszenie do wycieczki, aby móc dodawać posty.", exception.getMessage());
        verify(tripPostRepository, never()).save(any());
    }

    @Test
    void createPostWithPhotos_ShouldSavePostAndPhotos_WhenDataIsValid() {
        PostCreateRequest request = new PostCreateRequest(node.getId(), "Piękne widoki!");
        
        when(userRepository.findByEmail(author.getEmail())).thenReturn(Optional.of(author));
        when(tripNodeRepository.findById(node.getId())).thenReturn(Optional.of(node));

        TripParticipant acceptedParticipant = new TripParticipant();
        acceptedParticipant.setAccepted(true);
        when(tripParticipantRepository.findByEventIdAndUserId(event.getId(), author.getId()))
                .thenReturn(Optional.of(acceptedParticipant));

        MultipartFile mockFile = mock(MultipartFile.class);
        List<MultipartFile> files = List.of(mockFile);
        
        when(fileStorageService.savePhoto(mockFile)).thenReturn("fake-image.jpg");

        when(tripPostRepository.save(any(TripPost.class))).thenAnswer(invocation -> {
            TripPost savedPost = invocation.getArgument(0);
            savedPost.setId(UUID.randomUUID());
            return savedPost;
        });

        PostResponse response = tripPostService.createPostWithPhotos(request, files, author.getEmail());

        assertEquals("Piękne widoki!", response.note());
        assertEquals(author.getName(), response.reporterName());
        
        assertEquals(1, response.photos().size());
        assertEquals("http://localhost:8888/uploads/fake-image.jpg", response.photos().get(0).photoUrl());
        
        verify(tripPostRepository, times(1)).save(any(TripPost.class));
    }
}