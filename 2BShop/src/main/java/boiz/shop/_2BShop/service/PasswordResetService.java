package boiz.shop._2BShop.service;

import boiz.shop._2BShop.dto.ResetPasswordDTO;
import boiz.shop._2BShop.entity.PasswordResetToken;
import boiz.shop._2BShop.entity.User;
import boiz.shop._2BShop.respository.PasswordResetTokenRepository;
import boiz.shop._2BShop.respository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MailService mailService;

    /**
     * Create password reset token and send email
     */
    @Transactional
    public void createResetToken(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            // Don't reveal if email exists or not (security best practice)
            // Just silently return
            return;
        }

        User user = userOpt.get();

        // Delete any existing unused tokens for this user
        tokenRepository.deleteByUser(user);

        // Generate new token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1); // 1 hour expiry

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(expiryDate);
        resetToken.setUsed(false);

        tokenRepository.save(resetToken);

        // Send email
        mailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), token);
    }

    /**
     * Validate token
     */
    public boolean validateToken(String token) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = tokenOpt.get();
        return resetToken.isValid();
    }

    /**
     * Reset password using token
     */
    @Transactional
    public void resetPassword(ResetPasswordDTO dto) {
        PasswordResetToken resetToken = tokenRepository.findByToken(dto.getToken())
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ"));

        if (!resetToken.isValid()) {
            throw new RuntimeException("Token đã hết hạn hoặc đã được sử dụng");
        }

        if (!dto.isPasswordMatching()) {
            throw new RuntimeException("Mật khẩu mới và xác nhận không khớp");
        }

        // Update password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdatedDate(LocalDateTime.now());
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }

    /**
     * Clean up expired tokens (scheduled task can call this)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }
}
