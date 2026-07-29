package com.navrotskyi.trippyapi.dto.user;

import java.util.UUID;

public record InvitationDto(
        UUID tripId,
        String tripName,
        String inviterName,
        String role,
        boolean accepted
) {}
