package com.navrotskyi.trippyapi.dto;

import com.navrotskyi.trippyapi.validation.PasswordsMatch;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@PasswordsMatch
public record RegisterRequest (
    @NotBlank(message = "Imię nie może być puste")
    String name,

    @NotBlank(message = "Email nie może być pusty")
    @Email(message = "Poprawny adres email musi być podany")
    String email,

    @NotBlank(message = "Hasło nie może być puste")
    @Size(min = 8, max = 128, message = "Hasło powinno zawierać od 8 do 128 znaków")
    @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$!%*?&])[A-Za-z\\d@$!%*?&]+$",
                message = "Hasło ma zawierać przynajmniej jedną wielką, jedną małą literę, jedną cyfrę, oraz jeden specjalny znak"
    )
    String password,

    @NotBlank(message = "Prosimy potwierdzić hasło")
    String confirmPassword
) {

}
