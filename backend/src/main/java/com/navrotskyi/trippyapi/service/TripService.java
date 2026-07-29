package com.navrotskyi.trippyapi.service;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.navrotskyi.trippyapi.domain.TripEvent;
import com.navrotskyi.trippyapi.domain.TripParticipant;
import com.navrotskyi.trippyapi.domain.TripRole;
import com.navrotskyi.trippyapi.domain.User;
import com.navrotskyi.trippyapi.dto.trip.TripInviteRequest;
import com.navrotskyi.trippyapi.exception.ResourceNotFoundException;
import com.navrotskyi.trippyapi.repository.TripEventRepository;
import com.navrotskyi.trippyapi.repository.TripParticipantRepository;
import com.navrotskyi.trippyapi.repository.TripRoleRepository;
import com.navrotskyi.trippyapi.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class TripService {
        private final TripEventRepository tripEventRepository;
        private final UserRepository userRepository;
        private final TripParticipantRepository tripParticipantRepository;
        private final TripRoleRepository tripRoleRepository;

        public TripService(TripEventRepository tripEventRepository,
                        UserRepository userRepository,
                        TripParticipantRepository tripParticipantRepository,
                        TripRoleRepository tripRoleRepository) {
                this.tripEventRepository = tripEventRepository;
                this.userRepository = userRepository;
                this.tripParticipantRepository = tripParticipantRepository;
                this.tripRoleRepository = tripRoleRepository;
        }

        @Transactional
        public void inviteUserToTrip(UUID tripId,
                        TripInviteRequest inviteRequest,
                        String currentUsername) {
                TripEvent tripEvent = tripEventRepository.findById(tripId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Trip with id " + tripId + " not found"));

                if (!tripEvent.getOwner().getEmail().equals(currentUsername)) {
                        throw new AccessDeniedException("Only the owner of the trip can invite users");
                }

                User userToInvite = userRepository.findByEmail(inviteRequest.email())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User with email " + inviteRequest.email() + " not found"));

                boolean isAlreadyParticipant = tripParticipantRepository
                                .findByEventIdAndUserId(tripId, userToInvite.getId())
                                .isPresent();

                if (isAlreadyParticipant) {
                        throw new IllegalArgumentException("User with email " + inviteRequest.email()
                                        + " is already a participant of the trip");
                }

                TripRole participantRole = tripRoleRepository.findByName("PARTICIPANT")
                                .orElseThrow(() -> new ResourceNotFoundException("Role PARTICIPANT not found"));

                TripParticipant newParticipant = new TripParticipant();
                newParticipant.setEvent(tripEvent);
                newParticipant.setUser(userToInvite);
                newParticipant.setTripRole(participantRole);
                newParticipant.setAccepted(false);

                tripParticipantRepository.save(newParticipant);
        }

        @Transactional
        public void acceptInvitation(UUID tripId, String email) {
                TripParticipant participant = tripParticipantRepository
                                .findByUserEmailAndEventId(email, tripId)
                                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

                participant.setAccepted(true);
                tripParticipantRepository.save(participant);
        }

        @Transactional
        public void rejectInvitation(UUID tripId, String email) {
                TripParticipant participant = tripParticipantRepository
                                .findByUserEmailAndEventId(email, tripId)
                                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

                tripParticipantRepository.delete(participant);
        }
}
