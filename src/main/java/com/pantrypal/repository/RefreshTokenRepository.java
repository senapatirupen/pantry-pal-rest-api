package com.pantrypal.repository;

import com.pantrypal.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiryDate < :date OR r.revoked = true")
    void deleteExpiredTokens(@Param("date") LocalDateTime date);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(r) > 0 FROM RefreshToken r WHERE r.token = :token AND r.revoked = false AND r.expiryDate > :now")
    boolean isValidToken(@Param("token") String token, @Param("now") LocalDateTime now);
}
