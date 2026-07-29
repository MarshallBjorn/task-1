package com.navrotskyi.trippyapi.service;

import com.navrotskyi.trippyapi.domain.TokenBlacklist;
import com.navrotskyi.trippyapi.repository.RefreshTokenRepository;
import com.navrotskyi.trippyapi.repository.TokenBlacklistRepository;
import com.navrotskyi.trippyapi.repository.UserRepository;
import com.navrotskyi.trippyapi.security.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        final String jwt = authHeader.substring(7);

        final String userEmail;
        final Date expirationDate;
        try {
            userEmail = jwtService.extractUsername(jwt);
            expirationDate = jwtService.extractExpiration(jwt);
        } catch (JwtException e) {
            SecurityContextHolder.clearContext();
            return;
        }

        // Idempotentne dorzucenie do blacklisty.
        if (expirationDate != null && tokenBlacklistRepository.findByToken(jwt).isEmpty()) {
            tokenBlacklistRepository.save(TokenBlacklist.builder()
                    .token(jwt)
                    .expiryDate(expirationDate.toInstant())
                    .build());
        }

        if (userEmail != null) {
            userRepository.findByEmail(userEmail)
                    .ifPresent(refreshTokenRepository::deleteByUser);
        }

        SecurityContextHolder.clearContext();
    }
}