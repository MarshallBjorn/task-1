package com.navrotskyi.trippyapi.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integracyjny test CORS dla profilu prod.
 * Weryfikuje: dozwolone jest TYLKO origin z {@code app.frontend.url}; wszystkie inne blokowane.
 *
 * Spring Boot 4: {@code @AutoConfigureMockMvc} pochodzi z
 * {@code org.springframework.boot.webmvc.test.autoconfigure} (moduł spring-boot-starter-webmvc-test).
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
@AutoConfigureMockMvc
@ActiveProfiles("prod")
class CorsProdProfileTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${app.frontend.url}")
    private String allowedOrigin;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @Test
    @DisplayName("PROD CORS: preflight z dozwolonego origin → 200 + Access-Control-Allow-Origin")
    void shouldAllowCors_FromConfiguredFrontendUrl_OnProd() throws Exception {
        mockMvc.perform(options("/api/trips")
                        .header("Origin", allowedOrigin)
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", allowedOrigin));
    }

    @Test
    @DisplayName("PROD CORS: preflight z nieznanego origin → brak Access-Control-Allow-Origin (przeglądarka blokuje)")
    void shouldBlockCors_FromUnknownOrigin_OnProd() throws Exception {
        // Spring nie zawsze zwraca 403 dla invalid CORS (może być 200 bez header'a).
        // Z punktu widzenia security liczy się to, że response NIE MA Allow-Origin —
        // wtedy przeglądarka sama zablokuje request po stronie klienta.
        mockMvc.perform(options("/api/trips")
                        .header("Origin", "http://hacker-domain.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
