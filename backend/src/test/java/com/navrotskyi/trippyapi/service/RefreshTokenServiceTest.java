package com.navrotskyi.trippyapi.service;

import com.navrotskyi.trippyapi.domain.RefreshToken;
import com.navrotskyi.trippyapi.domain.User;
import com.navrotskyi.trippyapi.exception.TokenRefreshException;
import com.navrotskyi.trippyapi.repository.RefreshTokenRepository;
import com.navrotskyi.trippyapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", 604_800_000L);

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("u@trippy.pl");
    }

    // --- createRefreshToken ---

    @Test
    void createRefreshToken_shouldDeletePreviousAndPersistNewToken() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        RefreshToken result = refreshTokenService.createRefreshToken(user.getId());

        verify(refreshTokenRepository).deleteByUser(user);
        verify(refreshTokenRepository).flush();

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());

        RefreshToken saved = captor.getValue();
        assertEquals(user, saved.getUser());
        assertNotNull(saved.getToken());
        assertTrue(saved.getExpiryDate().isAfter(Instant.now()));
        assertSame(saved, result);
    }

    @Test
    void createRefreshToken_shouldThrow_whenUserNotFound() {
        UUID missingId = UUID.randomUUID();
        when(userRepository.findById(missingId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> refreshTokenService.createRefreshToken(missingId));

        assertTrue(ex.getMessage().contains(missingId.toString()));
        verify(refreshTokenRepository, never()).save(any());
        verify(refreshTokenRepository, never()).deleteByUser(any());
    }

    // --- rotateRefreshToken ---

    @Test
    void rotateRefreshToken_shouldDeleteOldAndReturnNewToken() {
        String oldTokenValue = "old-token-uuid";
        RefreshToken oldToken = RefreshToken.builder()
                .id(1L)
                .user(user)
                .token(oldTokenValue)
                .expiryDate(Instant.now().plusSeconds(60))
                .build();

        when(refreshTokenRepository.findByToken(oldTokenValue)).thenReturn(Optional.of(oldToken));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        RefreshToken result = refreshTokenService.rotateRefreshToken(oldTokenValue);

        // stary token musi zostać usunięty (poprzez deleteByUser w createRefreshToken)
        verify(refreshTokenRepository).deleteByUser(user);
        assertNotEquals(oldTokenValue, result.getToken(), "Nowy token musi się różnić od starego");
        assertEquals(user, result.getUser());
    }

    @Test
    void rotateRefreshToken_shouldThrow_whenTokenNotFound() {
        when(refreshTokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        TokenRefreshException ex = assertThrows(TokenRefreshException.class,
                () -> refreshTokenService.rotateRefreshToken("missing"));

        assertTrue(ex.getMessage().contains("Nie można znaleźć refresh tokena."));
        verify(refreshTokenRepository, never()).deleteByUser(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void rotateRefreshToken_shouldThrowAndDeleteToken_whenExpired() {
        String expiredValue = "expired-token";
        RefreshToken expired = RefreshToken.builder()
                .id(2L)
                .user(user)
                .token(expiredValue)
                .expiryDate(Instant.now().minusSeconds(60))
                .build();

        when(refreshTokenRepository.findByToken(expiredValue)).thenReturn(Optional.of(expired));

        TokenRefreshException ex = assertThrows(TokenRefreshException.class,
                () -> refreshTokenService.rotateRefreshToken(expiredValue));

        assertTrue(ex.getMessage().toLowerCase().contains("expired"));
        verify(refreshTokenRepository).delete(expired);
        verify(refreshTokenRepository, never()).save(any());
    }
}