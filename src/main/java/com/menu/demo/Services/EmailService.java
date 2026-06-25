package com.menu.demo.Services;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Réinitialisation de mot de passe — Code OTP");
            helper.setFrom("noreply@yourplatform.dz");

            String html = buildOtpEmailHtml(fullName, otp);
            helper.setText(html, true);   // true = isHtml

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send OTP email to: " + toEmail, e);
        }
    }

    private String buildOtpEmailHtml(String fullName, String otp) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 480px; margin: auto;
                        border: 1px solid #e0e0e0; border-radius: 8px; padding: 32px;">
                <h2 style="color: #2c3e50;">Réinitialisation de mot de passe</h2>
                <p>Bonjour <strong>%s</strong>,</p>
                <p>Votre code de vérification est :</p>
                <div style="font-size: 36px; font-weight: bold; letter-spacing: 12px;
                            color: #3498db; text-align: center; padding: 24px 0;">
                    %s
                </div>
                <p style="color: #7f8c8d; font-size: 13px;">
                    Ce code expire dans <strong>10 minutes</strong>.
                    Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.
                </p>
                <hr style="border: none; border-top: 1px solid #eee; margin: 24px 0;">
                <p style="color: #bdc3c7; font-size: 11px;">
                    School SaaS Platform — Ne répondez pas à cet email.
                </p>
            </div>
            """.formatted(fullName, otp);
    }
}
