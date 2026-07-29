package com.navrotskyi.trippyapi.dto.admin;

import com.navrotskyi.trippyapi.domain.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
    @NotBlank(message = "Imię/nickname nie może być puste")
    @Size(min = 2, max = 50, message = "Imię powinno się składać pomiędzy 2 a 50 znakami.")
    String name,

    @NotBlank(message = "Email nie może być pusty")
    @Email(message = "Niepoprawny format emaila")
    String email,

    @NotBlank(message = "Hasło jest wymagane")
    @Size(min = 8, max = 128, message = "Hasło powinno zawierać od 8 do 128 znaków")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$!%*?&])[A-Za-z\\d@$!%*?&]+$",
        message = "[ERROR] Password must contain at least one uppercase, one lowercase, one digit, and one special character"
    )
    String password,

    @NotNull(message = "[ERROR] Role must be defined.")
    Role role,

    @NotNull(message = "[ERROR] Verification status must be defined.")
    Boolean isVerified,

    @NotNull(message = "[ERROR] Blockade status must be defined.")
    Boolean isBlocked
) {
}
