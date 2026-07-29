package com.navrotskyi.trippyapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Pełna reprezentacja węzła wycieczki zwracana z operacji create/read-single/update.
 */
@Schema(description = "Szczegóły pojedynczego węzła wycieczki (elementu planu lub wydatku).")
public record TripNodeDto(

        @Schema(description = "Unikalny identyfikator węzła.", example = "3f0c2b7a-9d1e-4c2a-8f1b-0a1b2c3d4e5f")
        UUID id,

        @Schema(description = "Identyfikator wycieczki, do której należy węzeł.", example = "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d")
        UUID eventId,

        @Schema(description = "Identyfikator autora (reportera) węzła.", example = "9f8e7d6c-5b4a-3210-fedc-ba9876543210")
        UUID reporterId,

        @Schema(description = "Nazwa autora węzła.", example = "Anna Kowalska")
        String reporterName,

        @Schema(description = "Czas rozpoczęcia węzła.", example = "2025-07-01T09:00:00")
        LocalDateTime startTime,

        @Schema(description = "Czas zakończenia węzła.", example = "2025-07-01T11:30:00")
        LocalDateTime endTime,

        @Schema(description = "Nazwa węzła / tytuł wydatku.", example = "Lot do Barcelony")
        String name,

        @Schema(description = "Notatka do węzła.", example = "Bagaż rejestrowany wliczony w cenę.")
        String note,

        @Schema(description = "Cena/koszt węzła.", example = "249.99")
        BigDecimal price,

        @Schema(description = "Czy wydatek jest osobny (nie wchodzi do wspólnego bilansu).", example = "false")
        boolean separate,

        @Schema(description = "Czy bieżący użytkownik może edytować ten węzeł.", example = "true")
        boolean canEdit,

        @Schema(description = "Czy bieżący użytkownik może usunąć ten węzeł.", example = "true")
        boolean canDelete
) {}