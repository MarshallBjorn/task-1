package com.navrotskyi.trippyapi.controller.trip;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.navrotskyi.trippyapi.domain.User;
import com.navrotskyi.trippyapi.dto.trip.TripBalanceResponse;
import com.navrotskyi.trippyapi.service.trip.TripBalanceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Endpointy agregujące rozliczenia finansowe wycieczki — netto-bilanse uczestników
 * i zminimalizowana lista przelewów.
 */
@RestController
@RequestMapping("/api/trips")
@Tag(name = "Trip Balances", description = "Rozliczenia wycieczki: bilanse uczestników i lista przelewów minimalizująca liczbę transakcji.")
public class TripBalanceController {

    private final TripBalanceService tripBalanceService;

    public TripBalanceController(TripBalanceService tripBalanceService) {
        this.tripBalanceService = tripBalanceService;
    }

    @GetMapping("/{tripId}/balances")
    @Operation(
            summary = "Pobierz rozliczenia wycieczki",
            description = """
                    Wylicza i zwraca aktualne rozliczenia dla wycieczki:

                    1. **Bilanse uczestników** — dla każdego zaakceptowanego uczestnika netto-bilans
                       (dodatni = grupa winna mu pieniądze, ujemny = on winien grupie).
                       Wydatki z flagą `is_separate=true` są pomijane. Reszta jest dzielona po równo
                       na wszystkich zaakceptowanych uczestników, z deterministyczną dystrybucją
                       groszy resztkowych (np. 100 zł / 3 osoby → pierwsi otrzymują +0.01).

                    2. **Lista przelewów** — zminimalizowana liczba transakcji potrzebnych do
                       wyrównania wszystkich bilansów do zera. Algorytm hybrydowy: Simplified Debt Graph
                       (greedy) + DP na bitmasce dla N ≤ 15 uczestników (gwarancja matematycznego minimum).

                    **Dostęp:** tylko zaakceptowani uczestnicy wycieczki (`isAccepted=true`).
                    Użytkownik zaproszony ale niepotwierdzony dostaje 403. Bilanse liczone są na żywo —
                    każde wywołanie odzwierciedla aktualny stan wydatków w DB."""
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Bilanse i lista przelewów wyliczone pomyślnie.",
                    content = @Content(schema = @Schema(implementation = TripBalanceResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Brak autoryzacji — wymagany JWT.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Użytkownik nie jest zaakceptowanym uczestnikiem wycieczki.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Wycieczka o podanym `tripId` nie istnieje.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Niespójność danych (np. reporter node'a nie jest uczestnikiem) lub błąd serwera.",
                    content = @Content
            )
    })
    public ResponseEntity<TripBalanceResponse> getTripBalances(
            @PathVariable UUID tripId,
            @AuthenticationPrincipal User currentUser
    ) {
        TripBalanceResponse response = tripBalanceService.getBalances(tripId, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{tripId}/balances/export")
    @Operation(
    summary = "Eksportuj rozliczenia wycieczki do PDF",
    description = """
            Zwraca estetyczny raport PDF z bilansami uczestnikow i zminimalizowana
            lista przelewow. Dostep: tylko zaakceptowani uczestnicy wycieczki."""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plik PDF",
                    content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE)),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji", content = @Content),
            @ApiResponse(responseCode = "403", description = "Nie jestes uczestnikiem", content = @Content),
            @ApiResponse(responseCode = "404", description = "Wycieczka nie istnieje", content = @Content)
    })
    public ResponseEntity<ByteArrayResource> exportTripBalances(
            @PathVariable UUID tripId,
            @AuthenticationPrincipal User currentUser
    ) {
        byte[] pdf = tripBalanceService.exportBalancesPdf(tripId, currentUser);
        String filename = "rozliczenia-" + tripId + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(new ByteArrayResource(pdf));
    }
}
