package com.navrotskyi.trippyapi.service.email;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * Weryfikuje że na profilu dev kontekst wstrzykuje LocalFileEmailService
 * oraz że MailtrapEmailService (wymaga SMTP) nie trafia do kontekstu.
 */
@SpringBootTest
@ActiveProfiles("dev")
class EmailServiceDevProfileTest {

    @Autowired
    private EmailService emailService;

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("DEV: wstrzyknięta implementacja EmailService to LocalFileEmailService")
    void shouldLoadLocalFileEmailService_WhenProfileIsDev() {
        assertInstanceOf(LocalFileEmailService.class, emailService,
                "Na profilu DEV powinien załadować się LocalFileEmailService");
    }

    @Test
    @DisplayName("DEV: MailtrapEmailService NIE jest zarejestrowany w kontekście")
    void shouldNotContainMailtrapEmailService_WhenProfileIsDev() {
        assertFalse(
                context.getBeansOfType(MailtrapEmailService.class).size() > 0,
                "Na profilu DEV MailtrapEmailService nie powinien być beanem"
        );
    }
}
