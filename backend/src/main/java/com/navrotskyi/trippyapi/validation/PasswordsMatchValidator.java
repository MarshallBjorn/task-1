package com.navrotskyi.trippyapi.validation;

import com.navrotskyi.trippyapi.dto.RegisterRequest;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, RegisterRequest> {
    @Override
    public boolean isValid(RegisterRequest r, ConstraintValidatorContext ctx) {
        if (r.password() == null) return true;
        return r.password().equals(r.confirmPassword());
    }
}