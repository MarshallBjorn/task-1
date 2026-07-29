package com.navrotskyi.trippyapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidDateRangeValidator.class)
public @interface ValidDateRange {
    String message() default "Data zakończenia nie może być wcześniejsza niż data rozpoczęcia";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}