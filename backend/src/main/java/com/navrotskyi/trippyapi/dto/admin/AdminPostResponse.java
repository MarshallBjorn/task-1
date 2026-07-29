package com.navrotskyi.trippyapi.dto.admin;

import java.util.UUID;

public record AdminPostResponse(
        UUID id,
        UUID nodeId,
        String reporterName,
        String note
) {}