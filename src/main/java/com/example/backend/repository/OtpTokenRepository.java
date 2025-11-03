package com.example.backend.repository;

import com.example.backend.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findByToken(String token);
    Optional<OtpToken> findByEmailAndTypeAndUsedFalse(String email, OtpToken.OtpType type);
    Optional<OtpToken> findByEmailAndTokenAndType(String email, String token, OtpToken.OtpType type);
    void deleteByEmail(String email);
    void deleteByEmailAndUsedFalse(String email);
}
