package com.navrotskyi.trippyapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.navrotskyi.trippyapi.domain.User;
import com.navrotskyi.trippyapi.dto.AuthResponse;
import com.navrotskyi.trippyapi.dto.LoginRequest;
import com.navrotskyi.trippyapi.repository.UserRepository;
import com.navrotskyi.trippyapi.security.JwtService;
import com.navrotskyi.trippyapi.domain.RefreshToken;
import com.navrotskyi.trippyapi.domain.Role;

import org.mockito.Mockito;


@ExtendWith(MockitoExtension.class)
class AuthenticationServiceLoginTest {
    @Mock private UserRepository userRepository;
    @Mock private AuthenticationManager authManager;
    @Mock private JwtService jwtService;
    @Mock private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private LoginRequest request;

    @BeforeEach
    void SetUp() {
        request = new LoginRequest("john.doe@email.com", "redneck123!");
    }

    @Test
    void shouldSuccessfullyAuthenticateAndReturnJwt() {
        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setVerified(true);
        savedUser.setRole(Role.USER);

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(savedUser));
        when(jwtService.generateToken(any(User.class))).thenReturn("mock-jwt-token");

        RefreshToken mockRefreshToken = Mockito.mock(RefreshToken.class);
        when(mockRefreshToken.getToken()).thenReturn("mock-refresh-token");
        when(refreshTokenService.createRefreshToken(any())).thenReturn(mockRefreshToken);

        AuthResponse response = authenticationService.authenticate(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getAccessToken());
        
        verify(authManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotVerified() {
        when(authManager.authenticate(any()))
            .thenThrow(new DisabledException("Konto nie zostało zweryfikowane! Sprawdź swoją skrzynkę email."));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.authenticate(request);
        });

        assertEquals("Konto nie zostało zweryfikowane! Sprawdź swoją skrzynkę email.", exception.getMessage());

        verify(authManager, times(1)).authenticate(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void shouldPropagateExceptionWhenBadCredentials() {
        when(authManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> {
            authenticationService.authenticate(request);
        });

        verify(authManager, times(1)).authenticate(any());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundInDatabase() {
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> {
            authenticationService.authenticate(request);
        });

        verify(authManager, times(1)).authenticate(any());
        verify(jwtService, never()).generateToken(any());
    }
}