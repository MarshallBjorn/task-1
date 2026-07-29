package com.navrotskyi.trippyapi.dto.trip;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record TripInviteRequest(
        @NotBlank(message = "Email nie może być pusty")
        @Email(message = "Podaj poprawny format adresu email")
        String email
) {}