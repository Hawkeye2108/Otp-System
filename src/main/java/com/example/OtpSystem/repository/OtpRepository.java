package com.example.OtpSystem.repository;

import com.example.OtpSystem.model.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp,Long> {
    // Find the latest unverified OTP for an email
    Optional<Otp> findTopByEmailAndVerifiedFalseOrderByCreatedAtDesc(String email);

    // Find specific OTP by email and code
    Optional<Otp> findByEmailAndOtpCodeAndVerifiedFalse(String email, String otpCode);

    // Delete expired OTPs (for cleanup)
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
