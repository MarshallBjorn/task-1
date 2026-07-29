package com.navrotskyi.trippyapi.repository;

import com.navrotskyi.trippyapi.domain.TripEvent;
import com.navrotskyi.trippyapi.domain.TripParticipant;
import com.navrotskyi.trippyapi.domain.User;
import org.springframework.data.jpa.repository.EntityGraph; // DODAJ TEN IMPORT
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripParticipantRepository extends JpaRepository<TripParticipant, UUID> {

    @EntityGraph(attributePaths = { "user", "tripRole" })
    List<TripParticipant> findAllByEventId(UUID eventId);

    /**
     * Tylko zaakceptowani uczestnicy (isAccepted=true) — używane przez endpoint
     * balansów.
     */
    @EntityGraph(attributePaths = { "user" })
    List<TripParticipant> findAllByEventIdAndIsAcceptedTrue(UUID eventId);

    @EntityGraph(attributePaths = { "event", "event.owner", "tripRole" })
    List<TripParticipant> findByUserEmailAndIsAcceptedFalse(String email);

    Optional<TripParticipant> findByEventIdAndUserId(UUID eventId, UUID userId);

    Optional<TripParticipant> findByEventAndUser(TripEvent event, User user);

    Optional<TripParticipant> findByUserEmailAndEventId(String email, UUID eventId);
}