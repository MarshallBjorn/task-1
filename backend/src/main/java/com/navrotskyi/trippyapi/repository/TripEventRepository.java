package com.navrotskyi.trippyapi.repository;

import com.navrotskyi.trippyapi.domain.TripEvent;
import com.navrotskyi.trippyapi.domain.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;

@Repository
public interface TripEventRepository extends JpaRepository<TripEvent, UUID> {

        @EntityGraph(attributePaths = {
                        "participants",
                        "participants.user",
                        "participants.tripRole",
                        "currency"
        })
        List<TripEvent> findAllByOwnerId(UUID ownerId);

        @EntityGraph(attributePaths = {
                        "participants",
                        "participants.user",
                        "participants.tripRole",
                        "currency"
        })
        Optional<TripEvent> findById(UUID id);

        @Query("""
                        SELECT DISTINCT t FROM TripEvent t
                        LEFT JOIN FETCH t.participants p
                        LEFT JOIN FETCH p.user
                        LEFT JOIN FETCH p.tripRole
                        WHERE (p.user = :user AND p.isAccepted = true)
                           OR t.owner = :user
                        """)
        List<TripEvent> findAllUserTrips(@Param("user") User user);
}
