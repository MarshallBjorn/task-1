package com.navrotskyi.trippyapi.service;

import com.navrotskyi.trippyapi.dto.LoginRequest;
import com.navrotskyi.trippyapi.dto.AuthResponse;
import com.navrotskyi.trippyapi.dto.RegisterRequest;
import com.navrotskyi.trippyapi.exception.VerificationTokenExpiredException;
import com.navrotskyi.trippyapi.exception.VerificationTokenNotFoundException;
import com.navrotskyi.trippyapi.domain.RefreshToken;
import com.navrotskyi.trippyapi.domain.Role;
import com.navrotskyi.trippyapi.domain.User;
import com.navrotskyi.trippyapi.domain.VerificationToken;
import com.navrotskyi.trippyapi.repository.UserRepository;
import com.navrotskyi.trippyapi.repository.VerificationTokenRepository;
import com.navrotskyi.trippyapi.security.JwtService;
import com.navrotskyi.trippyapi.service.email.EmailService;

import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;

    public AuthResponse register(RegisterRequest request) {
        if (repository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        User user = new User(
                request.name(),
                request.email(),
                passwordEncoder.encode(request.password()),
                Role.USER,
                null,
                null,
                false,
                false
        );
        user.setVerified(false);
        var savedUser = repository.save(user);

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationToken.setUser(savedUser);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        tokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(savedUser.getEmail(), verificationToken.getToken());

        var jwtToken = jwtService.generateToken(savedUser);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser.getId());
        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    public AuthResponse authenticate(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );
        var user = repository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Nieprawidłowy adres email lub hasło."));

        var jwtToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken.getToken())
                .role(user.getAuthorities().stream().findFirst().map(Object::toString).orElse("USER"))
                .build();
    }

    @Transactional
    public void verifyEmail(String token) {
        VerificationToken vToken = tokenRepository.findByToken(token)
        .orElseThrow(() -> new VerificationTokenNotFoundException(
                        "Nieprawidłowy lub nieistniejący token weryfikacyjny."));

        if (vToken.getExpiryDate().isBefore(LocalDateTime.now())) {
        tokenRepository.delete(vToken);
        throw new VerificationTokenExpiredException(
                "Token weryfikacyjny wygasł. Zarejestruj się ponownie lub poproś o nowy link aktywacyjny.");
        }

        User user = vToken.getUser();
        user.setVerified(true);
        repository.save(user);
        tokenRepository.delete(vToken);
    }
}
