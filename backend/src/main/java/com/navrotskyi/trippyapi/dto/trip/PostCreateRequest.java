package com.navrotskyi.trippyapi.dto.trip;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record PostCreateRequest(
    @NotNull(message = "ID węzła jest wymagane.")
    UUID nodeId,
    
    @NotBlank(message = "Treść posta nie może być pusta.")
    @Size(max = 2000, message = "Treść posta nie może przekraczać 2000 znaków.")
    String note
) {}
