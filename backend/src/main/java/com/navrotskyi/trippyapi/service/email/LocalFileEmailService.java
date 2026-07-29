package com.navrotskyi.trippyapi.service.email;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Profile("dev")
public class LocalFileEmailService implements EmailService {
    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.email.local-dir:logs/emails}")
    private String localEmailDir;

    @Override
    public void sendVerificationEmail(String to, String token) {
        String verificationLink = frontendUrl + "/verify?token=" + token;

        String content = String.format("""
                ======================================
                DATA: %s
                DO: %s
                TEMAT: Weryfikacja konta - Trippy
                --------------------------------------
                Kliknij w poniższy link, aby zweryfikować konto:
                %s
                ======================================
                """, 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), 
                to, 
                verificationLink);

        try {
            Path directory = Paths.get(localEmailDir);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
            
            Path filePath = directory.resolve("verification_" + System.currentTimeMillis() + ".txt");
            Files.writeString(filePath, content);
            
            log.info("[DEV] Wygenerowano lokalny plik z mailem weryfikacyjnym: {}", filePath.toAbsolutePath());
        } catch (IOException e) {
            log.error("[DEV] Nie udało się zapisać maila do pliku!", e);
        }
    }
}
