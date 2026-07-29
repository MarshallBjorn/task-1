package com.navrotskyi.trippyapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InviteParticipantRequest (
    @NotBlank(message = "Email nie może być pusty")
    @Email(message = "Niepoprawny format emaila")
    String userEmail,

    @NotBlank(message = "Rola musi być ustalona")
    String roleName
) {

}