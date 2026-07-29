package com.navrotskyi.trippyapi.validation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidDateRangeValidator implements ConstraintValidator<ValidDateRange, DateRangeProvider> {
    @Override
    public boolean isValid(DateRangeProvider r, ConstraintValidatorContext ctx) {
        Temporal start = r.getRangeStart();
        Temporal end = r.getRangeEnd();
        if (start == null || end == null) return true;

        if (start instanceof LocalDate s && end instanceof LocalDate e) {
            return !e.isBefore(s);
        }

        if (start instanceof LocalDateTime s && end instanceof LocalDateTime e) {
            return !e.isBefore(s);
        }
        return true;
    }
}