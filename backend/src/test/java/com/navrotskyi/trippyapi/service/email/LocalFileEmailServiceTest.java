package com.navrotskyi.trippyapi.service.email;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit test dla dev-owej implementacji {@link LocalFileEmailService}.
 * Używa {@link TempDir} żeby katalog na maile był izolowany i posprzątany po teście.
 */
class LocalFileEmailServiceTest {

    private static final String FRONTEND_URL = "http://localhost:5173";

    @TempDir
    Path tempDir;

    private LocalFileEmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new LocalFileEmailService();
        ReflectionTestUtils.setField(emailService, "frontendUrl", FRONTEND_URL);
        ReflectionTestUtils.setField(emailService, "localEmailDir", tempDir.toString());
    }

    @Test
    @DisplayName("sendVerificationEmail: zapisuje plik .txt z treścią maila w skonfigurowanym katalogu")
    void shouldWriteEmailToLocalFileInConfiguredDir() throws IOException {
        // given
        String toEmail = "dev.user@trippy.local";
        String token = "dev-token-123";

        // when
        emailService.sendVerificationEmail(toEmail, token);

        // then
        List<Path> produced;
        try (var stream = Files.list(tempDir)) {
            produced = stream.toList();
        }

        assertEquals(1, produced.size(), "Powinien powstać dokładnie jeden plik z mailem");

        Path file = produced.get(0);
        String name = file.getFileName().toString();
        String content = Files.readString(file);

        assertAll("Zawartość pliku mail",
                () -> assertTrue(name.startsWith("verification_"), "Nazwa pliku powinna zaczynać się od 'verification_'"),
                () -> assertTrue(name.endsWith(".txt"), "Plik musi mieć rozszerzenie .txt"),
                () -> assertTrue(content.contains("DO: " + toEmail), "Treść musi zawierać adresata"),
                () -> assertTrue(content.contains("Weryfikacja konta - Trippy"), "Treść musi zawierać temat"),
                () -> assertTrue(content.contains(FRONTEND_URL + "/verify?token=" + token),
                        "Treść musi zawierać link z frontendUrl + token")
        );
    }

    @Test
    @DisplayName("sendVerificationEmail: tworzy brakujący katalog przed zapisem")
    void shouldCreateDirectoryIfMissing() throws IOException {
        // given — wskazujemy ścieżkę która jeszcze nie istnieje
        Path nested = tempDir.resolve("nested/does-not-exist-yet");
        ReflectionTestUtils.setField(emailService, "localEmailDir", nested.toString());

        // when
        emailService.sendVerificationEmail("test@example.com", "tok");

        // then
        assertTrue(Files.exists(nested), "Service powinien stworzyć brakujący katalog");
        try (var stream = Files.list(nested)) {
            assertEquals(1, stream.count(), "W nowo utworzonym katalogu powinien być 1 plik z mailem");
        }
    }
}
