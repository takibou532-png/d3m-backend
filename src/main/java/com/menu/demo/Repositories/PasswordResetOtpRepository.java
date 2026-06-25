package com.menu.demo.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.menu.demo.Models.PasswordResetOtp;
import com.menu.demo.Models.User;
@Repository
public interface PasswordResetOtpRepository
extends JpaRepository<PasswordResetOtp, Long> {

// Latest valid OTP for a user
Optional<PasswordResetOtp> findTopByUserAndUsedFalseOrderByCreatedAtDesc(User user);

// Cleanup old OTPs on new request
void deleteAllByUser(User user);
}