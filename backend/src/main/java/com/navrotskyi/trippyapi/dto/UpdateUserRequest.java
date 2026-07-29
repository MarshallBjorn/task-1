package com.navrotskyi.trippyapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(min = 2, max = 50, message = "[ERROR] Name must be between 2 and 50 characters")
        String name,

        @Email(message = "[ERROR] Email must be correct.")
        String email,

        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$!%*?&])[A-Za-z\\d@$!%*?&]+$",
                message = "[ERROR] Password must contain at least one uppercase, one lowercase, one digit, and one special character"
        )
        String password,

        String currentPassword,

        @Size(min = 3, max = 3, message = "Currency code expected")
        String currency_code
) {
}
