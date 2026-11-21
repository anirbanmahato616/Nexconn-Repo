package com.recoverx.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recoverx.auth.entity.PasswordResetToken;
import com.recoverx.auth.entity.User;
import com.recoverx.auth.repository.PasswordResetTokenRepository;
import com.recoverx.auth.repository.UserRepository;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String GENERIC_MESSAGE =
            "If an account exists for that email, a password reset link has been sent.";

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final String resetUrl;
    private final long tokenValidityMinutes;
    private final String mailUsername;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            JavaMailSender mailSender,
            @Value("${app.frontend.reset-password-url}") String resetUrl,
            @Value("${app.password-reset.token-validity-minutes:15}") long tokenValidityMinutes,
            @Value("${spring.mail.username}") String mailUsername) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.resetUrl = resetUrl;
        this.tokenValidityMinutes = tokenValidityMinutes;
        this.mailUsername = mailUsername;
    }

    @Transactional
    public String requestPasswordReset(String emailAddress) {
        String email = emailAddress.trim().toLowerCase(Locale.ROOT);
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return GENERIC_MESSAGE;
        }

        User user = optionalUser.get();
        tokenRepository.deleteAllByUserId(user.getId());

        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUserId(user.getId());
        resetToken.setTokenHash(sha256(rawToken));
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(tokenValidityMinutes));
        resetToken.setUsed(false);
        tokenRepository.save(resetToken);

        String link = resetUrl + (resetUrl.contains("?") ? "&" : "?") + "token=" + rawToken;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailUsername);
        message.setTo(user.getEmail());
        message.setSubject("RecoverX - Reset your password");

        message.setText(
                "Hello " + user.getFullName() + ",\n\n"
                + "We received a request to reset your RecoverX password.\n\n"
                + "Use the link below within " + tokenValidityMinutes + " minutes:\n\n"
                + link + "\n\n"
                + "If you did not request this, you can ignore this email.\n\n"
                + "RecoverX Team"
        );

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            // Keep the response generic even if delivery fails; never expose account existence.
            log.error("Unable to send password reset email. Check SMTP configuration.", ex);
        }

        return GENERIC_MESSAGE;
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        String tokenHash = sha256(rawToken);
        PasswordResetToken resetToken = tokenRepository.findByTokenHashAndUsedFalse(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired password reset link"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            resetToken.setUsed(true);
            tokenRepository.save(resetToken);
            throw new IllegalArgumentException("Invalid or expired password reset link");
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired password reset link"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        tokenRepository.deleteAllByUserId(user.getId());
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
