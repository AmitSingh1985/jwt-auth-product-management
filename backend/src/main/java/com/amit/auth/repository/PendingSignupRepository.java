package com.amit.auth.repository;

import com.amit.auth.entity.PendingSignup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PendingSignupRepository
        extends JpaRepository<PendingSignup, Long> {

    Optional<PendingSignup> findByEmail(String email);

    Optional<PendingSignup> findByUsername(String username);

    void deleteByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}

