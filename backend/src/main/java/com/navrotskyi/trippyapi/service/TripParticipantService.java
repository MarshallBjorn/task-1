package com.navrotskyi.trippyapi.service;

import com.navrotskyi.trippyapi.domain.*;
import com.navrotskyi.trippyapi.dto.InviteParticipantRequest;
import com.navrotskyi.trippyapi.exception.ResourceNotFoundException;
import com.navrotskyi.trippyapi.repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TripParticipantService {
    private final TripParticipantRepository tripParticipantRepository;
    private final TripEventRepository tripEventRepository;
    private final UserRepository userRepository;
    private final TripRoleRepository tripRoleRepository;

    @Transactional
    public TripParticipant inviteParticipant(UUID eventId, InviteParticipantRequest request, User inviter) {
        TripEvent event = tripEventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("TripEvent not found with id: " + eventId));

        if (!event.getOwner().getId().equals(inviter.getId())) {
            throw new SecurityException("Only the trip owner can invite participants.");
        }

        User userToInvite = userRepository.findByEmail(request.userEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.userEmail()));

        TripRole role = tripRoleRepository.findByName(request.roleName())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + request.roleName()));

        tripParticipantRepository.findByEventIdAndUserId(eventId, userToInvite.getId()).ifPresent(p -> {
            throw new IllegalStateException("User is already a participant in this trip.");
        });

        TripParticipant newParticipant = new TripParticipant(
                event,
                userToInvite,
                role,
                BigDecimal.ZERO,
                false
        );

        return tripParticipantRepository.save(newParticipant);
    }

    @Transactional(readOnly = true)
    public List<TripParticipant> getParticipantsForEvent(UUID eventId, User currentUser) {    
        if (!tripEventRepository.findById(eventId).isPresent()) {
            throw new ResourceNotFoundException("TripEvent not found with id: " + eventId);
        }

        boolean isActiveParticipant = tripParticipantRepository
                .findByEventIdAndUserId(eventId, currentUser.getId())
                .map(TripParticipant::isAccepted)
                .orElse(false);

        if (!isActiveParticipant) {
            throw new SecurityException("Only active trip participants can view the participants list.");
        }

        return tripParticipantRepository.findAllByEventId(eventId);
    }

    @Transactional
    public void removeTripParticipant(UUID eventId, UUID userToRemoveId, User currentUser) {
        TripEvent event = tripEventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("TripEvent not found with id: " + eventId));

        if (!event.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the trip owner can remove participants.");
        }

        if (event.getOwner().getId().equals(userToRemoveId)) {
            throw new IllegalStateException("Trip owner cannot be removed from the trip.");
        }

        TripParticipant participant = tripParticipantRepository.findByEventIdAndUserId(eventId, userToRemoveId)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Participant not found in trip " + eventId + " for user " + userToRemoveId));

        tripParticipantRepository.delete(participant);
    }
}