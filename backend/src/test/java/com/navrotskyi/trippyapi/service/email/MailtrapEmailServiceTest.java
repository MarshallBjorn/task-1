package com.navrotskyi.trippyapi.service.email;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit test dla prod-owej implementacji {@link MailtrapEmailService}.
 * Nie wstaje kontekst Spring — weryfikujemy kontrakt komponentu (co trafia do JavaMailSender).
 */
@ExtendWith(MockitoExtension.class)
class MailtrapEmailServiceTest {

    private static final String FRONTEND_URL = "https://trippy.example.com";

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private MailtrapEmailService emailService;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> messageCaptor;

    @BeforeEach
    void setUp() {
        // @Value('app.frontend.url') jest wstrzykiwany przez Springa w runtime
        // — w teście jednostkowym musimy ustawić go ręcznie.
        ReflectionTestUtils.setField(emailService, "frontendUrl", FRONTEND_URL);
    }

    @Test
    @DisplayName("sendVerificationEmail: buduje poprawną wiadomość i deleguje do JavaMailSender")
    void shouldBuildCorrectMessageAndDelegateSend() {
        // given
        String toEmail = "john.doe@example.com";
        String token = "mock-verification-token";

        // when
        emailService.sendVerificationEmail(toEmail, token);

        // then
        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage captured = messageCaptor.getValue();
        assertNotNull(captured, "Przechwycona wiadomość nie może być null");

        assertAll("SimpleMailMessage content",
                () -> assertEquals(toEmail, Objects.requireNonNull(captured.getTo())[0]),
                () -> assertEquals("Trippy - Potwierdź swój adres email", captured.getSubject()),
                () -> assertEquals("kontakt@tomoiolo.pl", captured.getFrom()),
                () -> assertTrue(
                        Objects.requireNonNull(captured.getText())
                                .contains(FRONTEND_URL + "/verify?token=" + token),
                        "Treść maila musi zawierać link aktywacyjny z frontendUrl + token"
                )
        );
    }
}
