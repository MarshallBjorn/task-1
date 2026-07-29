package com.navrotskyi.trippyapi.dto.trip;

import com.navrotskyi.trippyapi.validation.DateRangeProvider;
import com.navrotskyi.trippyapi.validation.ValidDateRange;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.Temporal;

@ValidDateRange
public record CreateTripEventRequest(
    @NotBlank(message = "Nazwa nie może być pusta")
    String name,

    @NotBlank(message = "Kod waluty nie może być pusty")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Kod waluty musi być w formacie ISO 4217 (3 wielkie litery)")
    String currencyCode,

    @NotNull(message = "Data rozpoczęcia jest wymagana")
    LocalDate startDate,

    @NotNull(message = "Data zakończenia jest wymagana")
    LocalDate endDate,

    @NotNull(message = "Budżet jest wymagany")
    @PositiveOrZero(message = "Budżet nie może być ujemny")
    BigDecimal budget
) implements DateRangeProvider {
    @Override public Temporal getRangeStart() { return startDate; }
    @Override public Temporal getRangeEnd() { return endDate; }
}