package com.navrotskyi.trippyapi.security;

import com.navrotskyi.trippyapi.repository.TokenBlacklistRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;

    @PostConstruct
    void validateJwtConfig() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException(
                "JWT secret key nie jest skonfigurowany. Ustaw zmienną środowiskową JWT_SECRET."
            );
        }

        try {
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            if (keyBytes.length < 32) {
                throw new IllegalStateException(
                    "JWT secret key jest za krótki. Algorytm HS256 wymaga klucza o długości co najmniej 32 bajtów po dekodowaniu base64. " +
                    "Aktualna długość: " + keyBytes.length + " bajtów."
                );
            }
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                "JWT secret key nie jest prawidłowym ciągiem base64.", ex);
        }

        if (jwtExpiration <= 0 ) {
            throw new IllegalStateException(
                "JWT expiration musi być dodatni. Aktualna wartość: " + jwtExpiration
            );
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SIG.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        boolean isTokenBlacklisted = tokenBlacklistRepository.findByToken(token).isPresent();
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token) && !isTokenBlacklisted;
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
