package com.navrotskyi.trippyapi.dto.admin;

import com.navrotskyi.trippyapi.domain.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @NotBlank(message = "Imię nie może być puste")
        @Size(min = 2, max = 50, message = "Imię musi mieć od 2 do 50 znaków")
        String name,

        @NotBlank(message = "Email nie może być pusty")
        @Email(message = "Niepoprawny format emaila")
        String email,

        @NotNull(message = "Rola musi być określona")
        Role role,

        @NotNull(message = "Status weryfikacji musi być określony")
        Boolean isVerified,

        @NotNull(message = "Status blokady musi być określony")
        Boolean isBlocked
) {
}