package com.akatsuki.auth.repository;

import com.akatsuki.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Optional<User> getUserByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
