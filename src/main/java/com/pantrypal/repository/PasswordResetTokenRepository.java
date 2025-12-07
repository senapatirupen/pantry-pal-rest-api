package com.pantrypal.repository;

import com.pantrypal.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiryDate < :date OR p.used = true")
    void deleteExpiredTokens(@Param("date") LocalDateTime date);

    @Modifying
    @Query("UPDATE PasswordResetToken p SET p.used = true WHERE p.token = :token")
    void markAsUsed(@Param("token") String token);
}
