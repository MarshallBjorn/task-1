package com.navrotskyi.trippyapi.dto.trip;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Odpowiedź endpointu {@code GET /api/trips/{tripId}/balances}.
 *
 * <p>Zawiera:</p>
 * <ul>
 *   <li>netto-bilans każdego zaakceptowanego uczestnika (kto ile "winien", kto "ma do otrzymania"),</li>
 *   <li>zminimalizowaną listę przelewów wyrównujących wszystkie bilanse do zera.</li>
 * </ul>
 *
 * <p>Bilanse liczone są na żywo — obejmują aktualny stan wydatków. Wycieczki nie są
 * "zamykane" w tej wersji API.</p>
 */
@Schema(description = "Podsumowanie rozliczeń wycieczki: bilanse uczestników + lista przelewów.")
public record TripBalanceResponse(

        @Schema(description = "UUID wycieczki")
        UUID tripId,

        @Schema(description = "Kod waluty wycieczki (ISO-4217)", example = "PLN")
        String currencyCode,

        @Schema(description = "Łączna kwota wydatków współdzielonych (bez tych z is_separate=true).",
                example = "1234.56")
        BigDecimal totalSharedExpenses,

        @Schema(description = "Bilanse wszystkich zaakceptowanych uczestników. "
                + "Sortowane po userId (deterministycznie).")
        List<ParticipantBalanceDto> balances,

        @Schema(description = "Zminimalizowana lista przelewów wyrównujących wszystkie bilanse do zera. "
                + "Pusta, gdy wszyscy są na zero.")
        List<SettlementDto> settlements
) {}
