package com.pantrypal.service;

import com.pantrypal.entity.RefreshToken;
import com.pantrypal.entity.User;
import com.pantrypal.exception.UnauthorizedException;
import com.pantrypal.repository.RefreshTokenRepository;
import com.pantrypal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    @Value("${app.jwt.refresh-expiration}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Revoke existing tokens for this user
        refreshTokenRepository.deleteByUserId(userId);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plus(refreshTokenDurationMs, ChronoUnit.MILLIS))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException("Refresh token not found"));
    }

    @Transactional
    public RefreshToken rotateRefreshToken(RefreshToken oldToken) {
        // Revoke old token
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        // Create new token
        return createRefreshToken(oldToken.getUser().getId());
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshToken -> {
                    refreshToken.setRevoked(true);
                    refreshTokenRepository.save(refreshToken);
                });
    }

    @Transactional
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        return refreshTokenRepository.isValidToken(token, LocalDateTime.now());
    }
}
