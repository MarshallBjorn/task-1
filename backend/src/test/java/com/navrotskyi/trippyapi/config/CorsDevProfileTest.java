package com.navrotskyi.trippyapi.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integracyjny test CORS dla profilu dev.
 * Weryfikuje: AllowedOriginPatterns("*") — dowolny origin przechodzi preflight.
 *
 * Spring Boot 4: {@code @AutoConfigureMockMvc} pochodzi z
 * {@code org.springframework.boot.webmvc.test.autoconfigure} (moduł spring-boot-starter-webmvc-test).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class CorsDevProfileTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("DEV CORS: preflight z localhost:5173 → 200 + echo Allow-Origin")
    void shouldAllowLocalhostOrigin_OnDev() throws Exception {
        mockMvc.perform(options("/api/trips")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    @DisplayName("DEV CORS: dowolne origin (np. z Postmana / test-host) też przechodzi")
    void shouldAllowAnyOrigin_OnDev() throws Exception {
        mockMvc.perform(options("/api/trips")
                        .header("Origin", "http://some-random-dev-host.local")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }
}
