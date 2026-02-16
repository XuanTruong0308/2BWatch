package boiz.shop._2BShop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import boiz.shop._2BShop.dto.RegisterDTO;
import boiz.shop._2BShop.entity.Role;
import boiz.shop._2BShop.entity.User;
import boiz.shop._2BShop.entity.UserRole;
import boiz.shop._2BShop.entity.VerificationToken;
import boiz.shop._2BShop.respository.RoleRepository;
import boiz.shop._2BShop.respository.UserRepository;
import boiz.shop._2BShop.respository.VerificationTokenRepository;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private VerificationTokenRepository tokenRepo;

    @Autowired
    private MailService mailService;

    @Transactional
    public void registerUser(RegisterDTO dto) {
        // Kiểm tra email đã tồn tại
        if (userRepo.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã được đăng ký!");
        }

        // Kiểm tra username đã tồn tại
        if (userRepo.findByUsername(dto.getUserName()).isPresent()) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }

        // Tạo user mới
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUserName());
        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setIsEnabled(false); // Chưa kích hoạt, chờ verify email
        
        // Lưu user trước
        User savedUser = userRepo.save(user);

        // Gán role USER (role_id = 2) cho user mới
        Role userRole = roleRepo.findByRoleName("USER")
                .orElseThrow(() -> new RuntimeException("Role USER không tồn tại!"));
        
        UserRole assignment = new UserRole();
        assignment.setUser(savedUser);
        assignment.setRole(userRole);
        savedUser.getUserRoles().add(assignment);
        
        // Lưu lại user với role đã gán
        userRepo.save(savedUser);

        // Tạo verification token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(savedUser);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        tokenRepo.save(verificationToken);

        // Gửi email xác thực
        try {
            mailService.sendRegistrationConfirmation(savedUser.getEmail(), savedUser.getFullName(), token);
        } catch (Exception e) {
            System.err.println("Lỗi gửi email: " + e.getMessage());
            // Không throw exception, vẫn cho phép đăng ký thành công
        }
    }

    @Transactional
    public boolean verifyEmail(String token, String email) {
        // Tìm verification token
        VerificationToken verificationToken = tokenRepo.findByToken(token)
                .orElse(null);

        if (verificationToken == null) {
            return false;
        }

        // Kiểm tra token đã hết hạn chưa
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        // Kiểm tra email khớp với user
        User user = verificationToken.getUser();
        if (!user.getEmail().equalsIgnoreCase(email)) {
            return false;
        }

        // Kích hoạt user
        user.setIsEnabled(true);
        user.setUpdatedDate(LocalDateTime.now());
        userRepo.save(user);

        // Xóa token đã sử dụng
        tokenRepo.delete(verificationToken);

        return true;
    }

    // Check if email exists
    public boolean existsByEmail(String email) {
        return userRepo.findByEmail(email).isPresent();
    }

    // Find user by email
    public java.util.Optional<User> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    // Verify email token (alias for verifyEmail)
    public boolean verifyEmailToken(String email, String token) {
        return verifyEmail(token, email);
    }

    // Activate user
    @Transactional
    public void activateUser(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user!"));
        user.setIsEnabled(true);
        user.setUpdatedDate(LocalDateTime.now());
        userRepo.save(user);
    }

    // Resend verification email
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user!"));

        // Delete old tokens
        tokenRepo.deleteByUserUserId(user.getUserId());

        // Create new token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        tokenRepo.save(verificationToken);

        // Send email
        mailService.sendRegistrationConfirmation(user.getEmail(), user.getFullName(), token);
    }

    // Send password reset email
    @Transactional
    public void sendPasswordResetEmail(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user!"));

        // Create reset token
        String token = UUID.randomUUID().toString();
        VerificationToken resetToken = new VerificationToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(2));
        tokenRepo.save(resetToken);

        // Send email
        mailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), token);
    }

    // Reset password
    @Transactional
    public void resetPassword(String token, String newPassword) {
        VerificationToken resetToken = tokenRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ!"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token đã hết hạn!");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedDate(LocalDateTime.now());
        userRepo.save(user);

        // Delete used token
        tokenRepo.delete(resetToken);
    }

    // Update profile
    @Transactional
    public void updateProfile(String email, String fullName, String phone, String address) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user!"));

        user.setFullName(fullName);
        user.setPhone(phone);
        user.setAddress(address);
        user.setUpdatedDate(LocalDateTime.now());
        userRepo.save(user);
    }

    // Change password
    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user!"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng!");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedDate(LocalDateTime.now());
        userRepo.save(user);
    }

    // Find all users with pagination
    public org.springframework.data.domain.Page<User> findAll(org.springframework.data.domain.Pageable pageable) {
        return userRepo.findAll(pageable);
    }

    // Search users by email or username
    public org.springframework.data.domain.Page<User> search(String keyword,
            org.springframework.data.domain.Pageable pageable) {
        // TODO: Add search method to UserRepository
        return userRepo.findAll(pageable);
    }

    // Find users by role
    public org.springframework.data.domain.Page<User> findByRole(String role,
            org.springframework.data.domain.Pageable pageable) {
        // TODO: Add findByRole method to UserRepository
        return userRepo.findAll(pageable);
    }

    // Find user by ID
    public java.util.Optional<User> findById(Integer id) {
        return userRepo.findById(id);
    }

    // Ban user
    @Transactional
    public void banUser(Integer userId, String reason, Integer violationTypeId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user!"));

        user.setIsEnabled(false);
        user.setUpdatedDate(LocalDateTime.now());
        userRepo.save(user);

        // TODO: Create BanLog entity
    }

    // Unban user
    @Transactional
    public void unbanUser(Integer userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user!"));

        user.setIsEnabled(true);
        user.setUpdatedDate(LocalDateTime.now());
        userRepo.save(user);
    }
}
