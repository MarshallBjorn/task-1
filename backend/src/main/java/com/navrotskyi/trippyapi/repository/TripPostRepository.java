package com.navrotskyi.trippyapi.repository;

import com.navrotskyi.trippyapi.domain.TripPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TripPostRepository extends JpaRepository<TripPost, UUID> {
    List<TripPost> findAllByNodeId(UUID nodeId);

    /**
     * Batch-fetch wszystkich postów dla zbioru nodeId z dociągniętym reporterem i zdjęciami.
     * Używane przez admina do generowania szczegółów wycieczki — 1 query zamiast N×M.
     *
     * <p>{@code distinct} bo JOIN po photos duplikuje wiersze posta.</p>
     */
    @Query("""
            SELECT DISTINCT p FROM TripPost p
            LEFT JOIN FETCH p.reporter
            LEFT JOIN FETCH p.photos
            WHERE p.node.id IN :nodeIds
            """)
    List<TripPost> findAllByNodeIdInWithReporterAndPhotos(@Param("nodeIds") Collection<UUID> nodeIds);
}