package com.menu.demo.Services;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.menu.demo.Exceptions.ResourceNotFoundException;
import com.menu.demo.Models.PasswordResetOtp;
import com.menu.demo.Models.User;
import com.menu.demo.Repositories.PasswordResetOtpRepository;
import com.menu.demo.Repositories.UserRepository;
import com.menu.demo.SecurityJwt.JwtUtill;

import Dto.ForgotPasswordRequest;
import Dto.OtpVerifiedResponse;
import Dto.ResetPasswordRequest;
import Dto.VerifyOtpRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtill jwtUtil;

    
    private int otpExpiryMinutes=5;

    // ======== STEP 1 — User requests OTP ========

    public void sendOtp(ForgotPasswordRequest request) {

        // Always return success even if email not found
        // → prevents email enumeration attacks
        User user = userRepository.findByEmail(request.getEmail())
            .orElse(null);

        if (user == null || !user.getEnabled()) return;

        // Delete any previous unused OTPs for this user
        otpRepository.deleteAllByUser(user);

        // Generate 6-digit OTP
        String otp = String.format("%06d",
            new java.util.Random().nextInt(999999));

        PasswordResetOtp entity = PasswordResetOtp.builder()
            .user(user)
            .otp(otp)
            .expiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
            .used(false)
            .build();

        otpRepository.save(entity);

        // Send email
        emailService.sendOtpEmail(user.getEmail(), otp, user.getFullName());
    }

    // ======== STEP 2 — User submits OTP → get reset token ========

    public OtpVerifiedResponse verifyOtp(VerifyOtpRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PasswordResetOtp otpRecord =
            otpRepository.findTopByUserAndUsedFalseOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new IllegalStateException("No active OTP found"));

        // Check expiry
        if (LocalDateTime.now().isAfter(otpRecord.getExpiresAt()))
            throw new IllegalStateException("OTP has expired. Please request a new one.");

        // Check code
        if (!otpRecord.getOtp().equals(request.getOtp()))
            throw new IllegalStateException("Invalid OTP code.");

        // Mark OTP as used
        otpRecord.setUsed(true);
        otpRepository.save(otpRecord);

        // Generate a short-lived reset token (JWT, 15 min)
        String resetToken = jwtUtil.generatePasswordResetToken(user);

        return OtpVerifiedResponse.builder()
            .resetToken(resetToken)
            .message("OTP verified. Use the reset token to set your new password.")
            .build();
    }

    // ======== STEP 3 — User sets new password ========

    public void resetPassword(ResetPasswordRequest request) {

        // Validate reset token
        if (!jwtUtil.isPasswordResetToken(request.getResetToken()))
            throw new IllegalStateException("Invalid or expired reset token.");

        String email = jwtUtil.extractEmail(request.getResetToken());

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (jwtUtil.isTokenExpired(request.getResetToken()))
            throw new IllegalStateException("Reset token has expired. Please start over.");

        // Validate new password
        if (request.getNewPassword() == null
                || request.getNewPassword().length() < 8)
            throw new IllegalArgumentException("Password must be at least 8 characters.");

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
