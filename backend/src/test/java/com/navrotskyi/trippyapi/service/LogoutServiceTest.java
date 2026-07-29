package com.navrotskyi.trippyapi.service;

import com.navrotskyi.trippyapi.domain.TokenBlacklist;
import com.navrotskyi.trippyapi.domain.User;
import com.navrotskyi.trippyapi.repository.RefreshTokenRepository;
import com.navrotskyi.trippyapi.repository.TokenBlacklistRepository;
import com.navrotskyi.trippyapi.repository.UserRepository;
import com.navrotskyi.trippyapi.security.JwtService;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    @Mock private TokenBlacklistRepository tokenBlacklistRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtService jwtService;
    @Mock private UserRepository userRepository;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;

    @InjectMocks
    private LogoutService logoutService;

    private User user;
    private final String validJwt = "valid.jwt.token";

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("u@trippy.pl");
    }

    @Test
    void logout_shouldBlacklistTokenAndDeleteRefreshToken_whenAuthHeaderValid() {
        Date expiry = new Date(System.currentTimeMillis() + 60_000);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + validJwt);
        when(jwtService.extractUsername(validJwt)).thenReturn(user.getEmail());
        when(jwtService.extractExpiration(validJwt)).thenReturn(expiry);
        when(tokenBlacklistRepository.findByToken(validJwt)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        logoutService.logout(request, response, null);

        ArgumentCaptor<TokenBlacklist> captor = ArgumentCaptor.forClass(TokenBlacklist.class);
        verify(tokenBlacklistRepository).save(captor.capture());
        assertEquals(validJwt, captor.getValue().getToken());
        assertEquals(expiry.toInstant(), captor.getValue().getExpiryDate());

        verify(refreshTokenRepository).deleteByUser(user);
        verify(response, never()).setStatus(anyInt());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void logout_shouldReturn401_whenAuthorizationHeaderMissing() {
        when(request.getHeader("Authorization")).thenReturn(null);

        logoutService.logout(request, response, null);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(tokenBlacklistRepository, never()).save(any());
        verify(refreshTokenRepository, never()).deleteByUser(any());
    }

    @Test
    void logout_shouldReturn401_whenAuthHeaderHasNoBearerPrefix() {
        when(request.getHeader("Authorization")).thenReturn("Basic xxx");

        logoutService.logout(request, response, null);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(tokenBlacklistRepository, never()).save(any());
    }

    @Test
    void logout_shouldNotBlacklistDuplicate_whenTokenAlreadyOnBlacklist() {
        Date expiry = new Date(System.currentTimeMillis() + 60_000);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + validJwt);
        when(jwtService.extractUsername(validJwt)).thenReturn(user.getEmail());
        when(jwtService.extractExpiration(validJwt)).thenReturn(expiry);
        when(tokenBlacklistRepository.findByToken(validJwt))
                .thenReturn(Optional.of(TokenBlacklist.builder().token(validJwt).build()));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        logoutService.logout(request, response, null);

        verify(tokenBlacklistRepository, never()).save(any());
        verify(refreshTokenRepository).deleteByUser(user);
    }

    @Test
    void logout_shouldReturnGracefully_whenJwtIsMalformed() {
        when(request.getHeader("Authorization")).thenReturn("Bearer broken");
        when(jwtService.extractUsername("broken")).thenThrow(new MalformedJwtException("bad"));

        logoutService.logout(request, response, null);

        verify(tokenBlacklistRepository, never()).save(any());
        verify(refreshTokenRepository, never()).deleteByUser(any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}