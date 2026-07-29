package com.navrotskyi.trippyapi.repository;

import com.navrotskyi.trippyapi.domain.TripEvent;
import com.navrotskyi.trippyapi.domain.TripNode;
import org.springframework.data.jpa.repository.EntityGraph; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripNodeRepository extends JpaRepository<TripNode, UUID> {
    
    @EntityGraph(attributePaths = {"reporter", "event"})
    List<TripNode> findAllByEventIdOrderByStartTimeAsc(UUID eventId);

    @EntityGraph(attributePaths = {"reporter", "event"})
    Optional<TripNode> findWithDetailsById(UUID id);

    @EntityGraph(attributePaths = {"reporter"})
    List<TripNode> findByEventId(UUID eventId);

    List<TripNode> findByEvent(TripEvent trip);
}