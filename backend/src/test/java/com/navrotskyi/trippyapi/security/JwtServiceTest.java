package com.navrotskyi.trippyapi.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import com.navrotskyi.trippyapi.repository.TokenBlacklistRepository;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.lang.Collections;

public class JwtServiceTest {
    private JwtService jwtService;
    private TokenBlacklistRepository tokenBlacklistRepository;
    private UserDetails testUser;

    @BeforeEach
    void SetUp() {
        tokenBlacklistRepository = Mockito.mock(TokenBlacklistRepository.class);
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "secretKey", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);
        ReflectionTestUtils.setField(jwtService, "tokenBlacklistRepository", tokenBlacklistRepository);

        testUser = new User("john.doe@email.com", "redneck123!", Collections.emptyList());
    }

    @Test
    void shouldGenerateTokenAndExtractUsername() {
        String token = jwtService.generateToken(testUser);

        assertNotNull(token);
        String extractedUsername = jwtService.extractUsername(token);
        assertEquals("john.doe@email.com", extractedUsername, "Subject w tokenie musi być równy emailowi!");
    }

    @Test
    void shouldReturnTrueWhenTokenIsValid() {
        Mockito.when(tokenBlacklistRepository.findByToken(Mockito.anyString())).thenReturn(java.util.Optional.empty());
        
        String token = jwtService.generateToken(testUser);

        boolean isValid = jwtService.isTokenValid(token, testUser);

        assertTrue(isValid, "Token powinien być w pełni walidny dla tego użytkownika");
    }

    @Test
    void shouldReturnFalseWhenTokenBelongsToAnotherUser() {
        Mockito.when(tokenBlacklistRepository.findByToken(Mockito.anyString())).thenReturn(java.util.Optional.empty());
        
        String token = jwtService.generateToken(testUser);
        UserDetails anotherUser = new User("jane.doe@email.com", "redneck123!", Collections.emptyList());
        
        boolean isValid = jwtService.isTokenValid(token, anotherUser);

        assertFalse(isValid, "Walidacja musi zawieść, jeśli token sprawdzany jest z innym emailem");
    }

    @Test 
    void shouldThrowExceptionWhenTokenIsExpired() throws InterruptedException {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1L);
        String token = jwtService.generateToken(testUser);

        Thread.sleep(10);

        assertThrows(ExpiredJwtException.class, () -> {
            jwtService.isTokenValid(token, testUser);
        }, "Serwis musi rzucić wyjątkiem ExpiredJwtException przy próbie parsowania starego tokena");
    }
}
