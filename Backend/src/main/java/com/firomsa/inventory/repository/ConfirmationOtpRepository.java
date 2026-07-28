package com.firomsa.inventory.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.firomsa.inventory.model.ConfirmationOTP;
import com.firomsa.inventory.model.User;

public interface ConfirmationOtpRepository extends JpaRepository<ConfirmationOTP, Integer> {

    void deleteAllByUser(User user);

    Optional<ConfirmationOTP> findByOtpAndExpiresAtAfterAndConfirmedFalse(String otp,
            LocalDateTime date);

    Optional<ConfirmationOTP> findByOtpAndUserEmailAndExpiresAtAfterAndConfirmedFalse(String otp,
            String email, LocalDateTime date);

    void deleteByUserEmailAndConfirmedFalse(String email);
}
