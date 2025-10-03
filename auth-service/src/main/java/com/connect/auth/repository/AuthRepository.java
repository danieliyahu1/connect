package com.akatsuki.auth.repository;

import com.akatsuki.auth.model.RefreshToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);

    void deleteByToken(String refreshToken);

    @Transactional
    void deleteByUser_Id(UUID userPk);

    Optional<RefreshToken> findByUser_Id(UUID userId);
}
