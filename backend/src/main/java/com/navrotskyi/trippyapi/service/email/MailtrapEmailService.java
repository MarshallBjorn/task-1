package com.navrotskyi.trippyapi.service.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("prod")
@Service
@RequiredArgsConstructor
public class MailtrapEmailService implements EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        String confirmationUrl = frontendUrl + "/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("kontakt@tomoiolo.pl");
        message.setTo(toEmail);
        message.setSubject("Trippy - Potwierdź swój adres email");
        message.setText("Witaj w Trippy! \n\n"
                + "Aby aktywować swoje konto i móc korzystać z aplikacji, kliknij w poniższy link:\n" 
                + confirmationUrl + "\n\n"
                + "Link jest ważny przez 24 godziny.");

        mailSender.send(message);
    }
}
