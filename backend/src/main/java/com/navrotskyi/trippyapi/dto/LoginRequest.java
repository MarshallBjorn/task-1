package com.navrotskyi.trippyapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest (
    @NotBlank(message = "Email nie może być pusty")
    @Email(message = "Niepoprawny format emaila")
    String email,

    @NotBlank(message = "Hasło nie może być puste")
    String password
) {

}
