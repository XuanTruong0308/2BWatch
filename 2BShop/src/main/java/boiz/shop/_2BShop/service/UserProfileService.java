package boiz.shop._2BShop.service;

import boiz.shop._2BShop.dto.ChangePasswordDTO;
import boiz.shop._2BShop.dto.UserProfileDTO;
import boiz.shop._2BShop.entity.User;
import boiz.shop._2BShop.respository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class UserProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FileUploadService fileUploadService;

    /**
     * Get user profile by ID
     */
    public UserProfileDTO getUserProfile(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        return convertToDTO(user);
    }

    /**
     * Update user profile
     */
    @Transactional
    public UserProfileDTO updateProfile(UserProfileDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Update fields
        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setUpdatedDate(LocalDateTime.now());

        // Note: Email cannot be changed (or implement email verification if allowing
        // change)

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    /**
     * Update avatar
     */
    @Transactional
    public String updateAvatar(Integer userId, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Upload avatar using FileUploadService
        String avatarUrl = fileUploadService.uploadUserAvatar(file, userId);

        user.setAvatarUrl(avatarUrl);
        user.setUpdatedDate(LocalDateTime.now());
        userRepository.save(user);

        return avatarUrl;
    }

    /**
     * Change password
     */
    @Transactional
    public void changePassword(ChangePasswordDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Verify current password
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng");
        }

        // Verify new password matches confirmation
        if (!dto.isPasswordMatching()) {
            throw new RuntimeException("Mật khẩu mới và xác nhận không khớp");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdatedDate(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Convert User entity to DTO
     */
    private UserProfileDTO convertToDTO(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setProvider(user.getProvider());  // OAuth2 provider
        dto.setPhoneVerified(user.getPhoneVerified());  // Phone verification status
        return dto;
    }
}
