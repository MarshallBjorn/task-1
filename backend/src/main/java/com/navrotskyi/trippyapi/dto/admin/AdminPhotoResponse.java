package com.navrotskyi.trippyapi.dto.admin;

import java.util.UUID;

public record AdminPhotoResponse(
        UUID id,
        UUID postId,
        String reporterName,
        String photoUrl
) {}