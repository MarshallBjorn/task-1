package com.navrotskyi.trippyapi.repository;

import com.navrotskyi.trippyapi.domain.TripRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripRoleRepository extends JpaRepository<TripRole, UUID> {
    Optional<TripRole> findByName(String name);
}