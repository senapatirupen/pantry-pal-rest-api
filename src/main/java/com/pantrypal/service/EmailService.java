package com.pantrypal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    @Value("${app.email.from:pantrypal@example.com}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.mock-email:true}")
    private boolean mockEmail;

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Async
    public void sendWelcomeEmail(String to, String username) {
        if (mockEmail) {
            log.info("[MOCK EMAIL] Welcome email sent to: {}", to);
            return;
        }

        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("appUrl", frontendUrl);

            String content = templateEngine.process("email/welcome", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Welcome to PantryPal!");
            helper.setText(content, true);

            mailSender.send(message);
            log.info("Welcome email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", to, e);
        }
    }

    @Async
    public void sendPasswordResetEmail(String to, String resetToken) {
        if (mockEmail) {
            log.info("[MOCK EMAIL] Password reset email sent to: {}", to);
            log.info("[MOCK EMAIL] Reset token: {}", resetToken);
            return;
        }

        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;

            Context context = new Context();
            context.setVariable("resetUrl", resetUrl);

            String content = templateEngine.process("email/password-reset", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Reset Your PantryPal Password");
            helper.setText(content, true);

            mailSender.send(message);
            log.info("Password reset email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", to, e);
        }
    }
}
