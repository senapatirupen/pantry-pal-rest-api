package com.pantrypal.service;


import com.pantrypal.dto.request.*;
import com.pantrypal.dto.response.AuthResponse;
import com.pantrypal.dto.response.UserResponse;
import com.pantrypal.entity.User;
import com.pantrypal.entity.RefreshToken;
import com.pantrypal.exception.*;
import com.pantrypal.repository.UserRepository;
import com.pantrypal.repository.RefreshTokenRepository;
import com.pantrypal.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());

        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already registered");
        }

        // Validate username uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ValidationException("Username already taken");
        }

        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getId());

        // Generate tokens
        String token = jwtUtils.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user.getId()).getToken();

        // Send welcome email (async)
        emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());

        return buildAuthResponse(user, token, refreshToken);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get user details
            User user = (User) authentication.getPrincipal();

            // Generate tokens
            String token = jwtUtils.generateToken(user);
            String refreshToken = refreshTokenService.createRefreshToken(user.getId()).getToken();

            log.info("User logged in successfully: {}", user.getId());
            return buildAuthResponse(user, token, refreshToken);

        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Invalid email or password");
        }
    }

    @Transactional
    public void logout(String refreshToken) {
        log.info("Logging out user with refresh token");
        refreshTokenService.revokeRefreshToken(refreshToken);
        SecurityContextHolder.clearContext();
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Refreshing token");

        RefreshToken token = refreshTokenService.findByToken(refreshToken);

        if (token.isRevoked() || token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Refresh token is expired or revoked");
        }

        User user = token.getUser();
        String newAccessToken = jwtUtils.generateToken(user);
        String newRefreshToken = refreshTokenService.rotateRefreshToken(token).getToken();

        log.info("Token refreshed for user: {}", user.getId());
        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        log.info("Processing forgot password for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Email not found"));

        // Create password reset token
        String resetToken = UUID.randomUUID().toString();
        // Save to database (implementation omitted for brevity)

        // Send reset email
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);

        log.info("Password reset email sent to: {}", request.getEmail());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Processing password reset");

        // Validate token and update password (implementation omitted for brevity)
        // This would involve finding the PasswordResetToken, checking expiry,
        // updating user password, and marking token as used

        log.info("Password reset successful");
    }

    @Transactional(readOnly = true)
    public UserResponse verifyToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Invalid or expired token");
        }

        User user = (User) authentication.getPrincipal();
        return mapToUserResponse(user);
    }

    private AuthResponse buildAuthResponse(User user, String token, String refreshToken) {
        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(mapToUserResponse(user))
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
