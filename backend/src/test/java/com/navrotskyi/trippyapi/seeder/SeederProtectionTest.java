package com.navrotskyi.trippyapi.seeder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Weryfikuje że na profilu prod:
 *  - DataInitializer (CommandLineRunner siejący bazę) NIE jest w kontekście,
 *  - SeederGuardConfig jest aktywny (defense in depth).
 *
 * JavaMailSender jest mockowany bo prod-owy kontekst aktywuje MailtrapEmailService
 * a my nie mamy realnego SMTP w testach.
 */
@SpringBootTest(properties = {
        "app.frontend.url=https://trippy.example.com",
        "app.file-server.url=https://trippy.example.com/uploads/",
        "application.security.jwt.secret-key=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
        "spring.mail.host=localhost",
        "spring.mail.port=1025",
        "spring.mail.username=test-user",
        "spring.mail.password=test-password"
})
@ActiveProfiles("prod")
class SeederProtectionTest {

    @Autowired
    private ApplicationContext context;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @Test
    @DisplayName("PROD: DataInitializer (seeder entrypoint) nie istnieje w kontekście")
    void dataInitializer_ShouldNotBeLoaded_OnProdProfile() {
        assertTrue(
                context.getBeansOfType(DataInitializer.class).isEmpty(),
                "Na profilu PROD DataInitializer musi być wyłączony — @Profile(\"dev\")"
        );
    }

    @Test
    @DisplayName("PROD: SeederGuardConfig jest aktywny (startował bez wyjątku)")
    void seederGuardConfig_ShouldBeActive_OnProdProfile() {
        // Jeśli SeederGuardConfig wykryłby DataInitializer, rzuciłby IllegalStateException
        // w @PostConstruct i kontekst by się nie podniósł. Skoro tu jesteśmy — guard zadziałał.
        assertTrue(
                context.containsBean("seederGuardConfig"),
                "SeederGuardConfig powinien być zarejestrowany na profilu prod"
        );
    }
}
