package com.navrotskyi.trippyapi.dto.trip;

import java.math.BigDecimal;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Pojedynczy przelew z listy rozliczeń: {@code from} → {@code to}, kwota {@code amount}.
 *
 * <p>Wzbogacony o nazwy użytkowników, żeby frontend nie musiał dla każdego przelewu
 * odpytywać {@code /api/users/{id}}.</p>
 */
@Schema(description = "Pojedynczy przelew rozliczeniowy.")
public record SettlementDto(

        @Schema(description = "UUID płatnika (dłużnika)", example = "b1f3a2d4-5e6f-4789-abcd-1234567890ab")
        UUID fromUserId,

        @Schema(description = "Nazwa płatnika", example = "Ola Nowak")
        String fromUserName,

        @Schema(description = "UUID odbiorcy (wierzyciela)", example = "c2d4e5f6-7890-4abc-def1-234567890abc")
        UUID toUserId,

        @Schema(description = "Nazwa odbiorcy", example = "Jan Kowalski")
        String toUserName,

        @Schema(description = "Kwota przelewu w walucie wycieczki (zawsze dodatnia, scale=2).",
                example = "33.33")
        BigDecimal amount
) {}
