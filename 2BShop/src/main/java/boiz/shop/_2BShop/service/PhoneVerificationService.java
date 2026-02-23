package boiz.shop._2BShop.service;

import boiz.shop._2BShop.entity.User;
import boiz.shop._2BShop.respository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PhoneVerificationService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Update phone and auto-verify (simplified version without OTP)
     * For production: implement SMS OTP verification
     */
    @Transactional
    public void updatePhoneAndVerify(User user, String phone) {
        if (phone == null || phone.isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống");
        }

        // Validate phone format (Vietnamese phone numbers)
        if (!phone.matches("^(0|84)(3|5|7|8|9)[0-9]{8}$")) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ (VD: 0912345678)");
        }

        user.setPhone(phone);
        user.setPhoneVerified(true);
        user.setUpdatedDate(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Check if user needs phone verification
     */
    public boolean needsPhoneVerification(User user) {
        // OAuth2 users (Google) must have verified phone to checkout
        if ("GOOGLE".equals(user.getProvider())) {
            return user.getPhone() == null || user.getPhone().isEmpty() || !user.getPhoneVerified();
        }
        return false;
    }
}
