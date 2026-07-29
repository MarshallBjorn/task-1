package com.navrotskyi.trippyapi.repository;

import com.navrotskyi.trippyapi.domain.TripPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TripPhotoRepository extends JpaRepository<TripPhoto, UUID> {
    List<TripPhoto> findAllByPostId(UUID postId);
}