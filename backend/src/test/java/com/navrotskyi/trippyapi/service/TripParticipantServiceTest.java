package com.navrotskyi.trippyapi.service;

import com.navrotskyi.trippyapi.domain.TripEvent;
import com.navrotskyi.trippyapi.domain.TripParticipant;
import com.navrotskyi.trippyapi.domain.TripRole;
import com.navrotskyi.trippyapi.domain.User;
import com.navrotskyi.trippyapi.exception.ResourceNotFoundException;
import com.navrotskyi.trippyapi.repository.TripEventRepository;
import com.navrotskyi.trippyapi.repository.TripParticipantRepository;
import com.navrotskyi.trippyapi.repository.TripRoleRepository;
import com.navrotskyi.trippyapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripParticipantServiceTest {

    @Mock private TripParticipantRepository tripParticipantRepository;
    @Mock private TripEventRepository tripEventRepository;
    @Mock private UserRepository userRepository;
    @Mock private TripRoleRepository tripRoleRepository;

    @InjectMocks
    private TripParticipantService tripParticipantService;

    private User owner;
    private User participantUser;
    private User stranger;
    private TripEvent event;
    private TripRole role;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setEmail("owner@trippy.pl");

        participantUser = new User();
        participantUser.setId(UUID.randomUUID());
        participantUser.setEmail("p@trippy.pl");

        stranger = new User();
        stranger.setId(UUID.randomUUID());
        stranger.setEmail("stranger@trippy.pl");

        event = new TripEvent();
        event.setId(UUID.randomUUID());
        event.setOwner(owner);

        role = new TripRole();
        role.setId(UUID.randomUUID());
        role.setName("Member");
    }

    private TripParticipant participant(User u, boolean accepted) {
        return new TripParticipant(event, u, role, BigDecimal.ZERO, accepted);
    }

    // ---------- removeParticipant ----------

    @Test
    void removeParticipant_shouldDeleteParticipant_whenCalledByOwner() {
        TripParticipant existing = participant(participantUser, true);

        when(tripEventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(tripParticipantRepository.findByEventIdAndUserId(event.getId(), participantUser.getId()))
                .thenReturn(Optional.of(existing));

        tripParticipantService.removeTripParticipant(event.getId(), participantUser.getId(), owner);

        verify(tripParticipantRepository).delete(existing);
    }

    @Test
    void removeParticipant_shouldThrowSecurity_whenCallerIsNotOwner() {
        when(tripEventRepository.findById(event.getId())).thenReturn(Optional.of(event));

        assertThrows(AccessDeniedException.class,
                () -> tripParticipantService.removeTripParticipant(event.getId(), participantUser.getId(), stranger));

        verify(tripParticipantRepository, never()).delete(any());
    }

    @Test
    void removeParticipant_shouldThrowIllegalState_whenRemovingOwnerThemselves() {
        when(tripEventRepository.findById(event.getId())).thenReturn(Optional.of(event));

        assertThrows(IllegalStateException.class,
                () -> tripParticipantService.removeTripParticipant(event.getId(), owner.getId(), owner));

        verify(tripParticipantRepository, never()).findByEventIdAndUserId(any(), any());
        verify(tripParticipantRepository, never()).delete(any());
    }

    @Test
    void removeParticipant_shouldThrowNotFound_whenEventDoesNotExist() {
        UUID missingEventId = UUID.randomUUID();
        when(tripEventRepository.findById(missingEventId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> tripParticipantService.removeTripParticipant(missingEventId, participantUser.getId(), owner));

        verify(tripParticipantRepository, never()).delete(any());
    }

    @Test
    void removeParticipant_shouldThrowNotFound_whenParticipantDoesNotExist() {
        when(tripEventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(tripParticipantRepository.findByEventIdAndUserId(event.getId(), participantUser.getId()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> tripParticipantService.removeTripParticipant(event.getId(), participantUser.getId(), owner));

        verify(tripParticipantRepository, never()).delete(any());
    }

    // ---------- getParticipantsForEvent (guard) ----------

    @Test
    void getParticipantsForEvent_shouldReturnList_whenCallerIsAcceptedParticipant() {
        TripParticipant ownerParticipant = participant(owner, true);
        TripParticipant otherAccepted = participant(participantUser, true);

        when(tripEventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(tripParticipantRepository.findByEventIdAndUserId(event.getId(), owner.getId()))
                .thenReturn(Optional.of(ownerParticipant));
        when(tripParticipantRepository.findAllByEventId(event.getId()))
                .thenReturn(List.of(ownerParticipant, otherAccepted));

        List<TripParticipant> result = tripParticipantService.getParticipantsForEvent(event.getId(), owner);

        assertEquals(2, result.size());
    }

    @Test
    void getParticipantsForEvent_shouldThrowSecurity_whenCallerIsPendingParticipant() {
        TripParticipant pending = participant(participantUser, false);

        when(tripEventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(tripParticipantRepository.findByEventIdAndUserId(event.getId(), participantUser.getId()))
                .thenReturn(Optional.of(pending));

        assertThrows(SecurityException.class,
                () -> tripParticipantService.getParticipantsForEvent(event.getId(), participantUser));

        verify(tripParticipantRepository, never()).findAllByEventId(any());
    }

    @Test
    void getParticipantsForEvent_shouldThrowSecurity_whenCallerIsStranger() {
        when(tripEventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(tripParticipantRepository.findByEventIdAndUserId(event.getId(), stranger.getId()))
                .thenReturn(Optional.empty());

        assertThrows(SecurityException.class,
                () -> tripParticipantService.getParticipantsForEvent(event.getId(), stranger));

        verify(tripParticipantRepository, never()).findAllByEventId(any());
    }

    @Test
    void getParticipantsForEvent_shouldThrowNotFound_whenEventDoesNotExist() {
        UUID missingEventId = UUID.randomUUID();
        when(tripEventRepository.findById(missingEventId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> tripParticipantService.getParticipantsForEvent(missingEventId, owner));

        verify(tripParticipantRepository, never()).findAllByEventId(any());
    }
}