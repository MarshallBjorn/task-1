package com.navrotskyi.trippyapi.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Reprezentacja węzła zwracana w listach (np. szczegóły wycieczki w panelu admina
 * oraz lista węzłów wycieczki dla uczestnika).
 */
@Schema(description = "Węzeł wycieczki wraz z powiązanymi postami i uprawnieniami bieżącego użytkownika.")
public record NodeResponse(

        @Schema(description = "Unikalny identyfikator węzła.", example = "3f0c2b7a-9d1e-4c2a-8f1b-0a1b2c3d4e5f")
        UUID id,

        @Schema(description = "Nazwa węzła / tytuł wydatku.", example = "Lot do Barcelony")
        String name,

        @Schema(description = "Notatka do węzła.", example = "Bagaż rejestrowany wliczony w cenę.")
        String note,

        @Schema(description = "Cena/koszt węzła.", example = "249.99")
        BigDecimal price,

        @Schema(description = "Czy wydatek jest osobny (poza wspólnym bilansem).", example = "false")
        boolean isSeparate,

        @Schema(description = "Nazwa autora węzła.", example = "Anna Kowalska")
        String reporterName,

        @Schema(description = "Posty powiązane z węzłem (puste w listingu uczestnika).")
        List<PostResponse> posts,

        @Schema(description = "Czy bieżący użytkownik może edytować ten węzeł.", example = "true")
        boolean canEdit,

        @Schema(description = "Czy bieżący użytkownik może usunąć ten węzeł.", example = "true")
        boolean canDelete
) {}