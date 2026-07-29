package com.navrotskyi.trippyapi.dto.trip;

import com.navrotskyi.trippyapi.validation.DateRangeProvider;
import com.navrotskyi.trippyapi.validation.ValidDateRange;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;

/**
 * Request DTO dla tworzenia oraz aktualizowania węzła wycieczki ({@code TripNode}).
 * <p>
 * Obsługuje również wydatki — wydatek to węzeł z dodatnią ceną i flagą {@code isSeparate}.
 * <p>
 * Walidacja pól sprawdzana jest na poziomie kontrolera przez {@code @Valid}.
 * Walidacja wzajemnej spójności pól (endTime &gt; startTime) realizowana jest przez
 * {@link ValidDateRange} oraz dodatkowo w warstwie serwisu.
 */
@ValidDateRange
@Schema(description = "Dane wejściowe do utworzenia lub aktualizacji węzła wycieczki (elementu planu lub wydatku).")
public record CreateTripNodeRequest(

        @Schema(description = "Czas rozpoczęcia węzła (ISO-8601).", example = "2025-07-01T09:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Pole startTime jest wymagane.")
        LocalDateTime startTime,

        @Schema(description = "Czas zakończenia węzła (ISO-8601). Musi być późniejszy niż startTime.", example = "2025-07-01T11:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Pole endTime jest wymagane.")
        LocalDateTime endTime,

        @Schema(description = "Nazwa węzła / tytuł wydatku.", example = "Lot do Barcelony", maxLength = 200, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Nazwa węzła nie może być pusta.")
        @Size(max = 200, message = "Nazwa węzła może mieć maksymalnie 200 znaków.")
        String name,

        @Schema(description = "Opcjonalna notatka do węzła.", example = "Bagaż rejestrowany wliczony w cenę.", maxLength = 5000)
        @Size(max = 5000, message = "Notatka może mieć maksymalnie 5000 znaków.")
        String note,

        @Schema(description = "Cena/koszt węzła. Użyj 0 jeśli węzeł nie wiąże się z kosztem.", example = "249.99", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Pole price jest wymagane (użyj 0 jeśli węzeł nie wiąże się z kosztem).")
        @DecimalMin(value = "0.00", inclusive = true, message = "Cena nie może być ujemna.")
        @Digits(integer = 10, fraction = 2, message = "Cena może mieć maksymalnie 10 cyfr przed przecinkiem i 2 po przecinku.")
        BigDecimal price,

        @Schema(description = "Czy wydatek jest osobny (nie wchodzi do wspólnego bilansu rozliczeń).", example = "false")
        boolean isSeparate

) implements DateRangeProvider {

    @Override
    public Temporal getRangeStart() {
        return startTime();
    }

    @Override
    public Temporal getRangeEnd() {
        return endTime();
    }
}