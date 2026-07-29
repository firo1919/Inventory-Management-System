package com.firomsa.inventory.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.firomsa.inventory.model.RefreshToken;
import com.firomsa.inventory.model.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    void deleteAllByUser(User user);

    Optional<RefreshToken> findByToken(String refreshToken);

    Optional<RefreshToken> findByTokenAndUser(String refreshToken, User user);
}
