package com.navrotskyi.trippyapi.dto.trip;

import java.math.BigDecimal;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Netto-bilans uczestnika wycieczki.
 *
 * <p>Dodatni {@code netBalance} = grupa jest winna temu uczestnikowi (dopłacił ze swoich).
 * Ujemny {@code netBalance} = uczestnik jest winien grupie.</p>
 */
@Schema(description = "Netto-bilans jednego uczestnika wycieczki.")
public record ParticipantBalanceDto(

        @Schema(description = "UUID uczestnika", example = "b1f3a2d4-5e6f-4789-abcd-1234567890ab")
        UUID userId,

        @Schema(description = "Imię/nazwa uczestnika", example = "Jan Kowalski")
        String userName,

        @Schema(description = "Netto-bilans (waluta wycieczki). Dodatni = grupa winna mu, ujemny = on winien grupie.",
                example = "66.67")
        BigDecimal netBalance
) {}
