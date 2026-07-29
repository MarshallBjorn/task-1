package com.navrotskyi.trippyapi.dto.admin;

import java.util.List;
import java.util.UUID;

public record PostResponse(UUID id, String note, String reporterName, List<PhotoResponse> photos) {}