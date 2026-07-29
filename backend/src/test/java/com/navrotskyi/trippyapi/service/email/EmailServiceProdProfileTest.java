package com.navrotskyi.trippyapi.service.email;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Weryfikuje że na profilu prod kontekst wstrzykuje MailtrapEmailService
 * oraz że LocalFileEmailService w ogóle nie jest zarejestrowany jako bean.
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
class EmailServiceProdProfileTest {

    @Autowired
    private EmailService emailService;

    @Autowired
    private ApplicationContext context;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @Test
    @DisplayName("PROD: wstrzyknięta implementacja EmailService to MailtrapEmailService")
    void shouldLoadMailtrapEmailService_WhenProfileIsProd() {
        assertInstanceOf(MailtrapEmailService.class, emailService,
                "Na profilu PROD powinien załadować się MailtrapEmailService");
    }

    @Test
    @DisplayName("PROD: LocalFileEmailService NIE jest zarejestrowany w kontekście")
    void shouldNotContainLocalFileEmailService_WhenProfileIsProd() {
        assertFalse(
                context.getBeansOfType(LocalFileEmailService.class).size() > 0,
                "Na profilu PROD LocalFileEmailService nie powinien być beanem"
        );
    }
}
