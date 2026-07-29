package com.navrotskyi.trippyapi.dto.admin;

import java.util.UUID;

import com.navrotskyi.trippyapi.domain.Role;

public record UserAdminResponse(
        UUID id,
        String name,
        String email,
        Role role,
        boolean isVerified,
        boolean isBlocked,
        String photoUrl
) {
}