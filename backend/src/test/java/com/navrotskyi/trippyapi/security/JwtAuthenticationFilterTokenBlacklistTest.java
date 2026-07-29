package com.navrotskyi.trippyapi.security;

import com.navrotskyi.trippyapi.domain.TokenBlacklist;
import com.navrotskyi.trippyapi.repository.TokenBlacklistRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterBlacklistTest {

    @Mock private JwtService jwtService;
    @Mock private UserDetailsService userDetailsService;
    @Mock private TokenBlacklistRepository tokenBlacklistRepository;
    @Mock private FilterChain filterChain;

    @Test
    void shouldReturn401_whenTokenIsBlacklisted() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                jwtService, userDetailsService, tokenBlacklistRepository);

        String jwt = "blacklisted.jwt";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + jwt);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenBlacklistRepository.findByToken(jwt))
                .thenReturn(Optional.of(TokenBlacklist.builder().token(jwt).build()));

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("jest zablokowany"));
        verify(filterChain, never()).doFilter(any(), any());
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void shouldPassThrough_whenNoAuthorizationHeader() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                jwtService, userDetailsService, tokenBlacklistRepository);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(tokenBlacklistRepository, jwtService, userDetailsService);
    }
}