# 📱 PHONE VERIFICATION FOR OAUTH2 USERS - IMPLEMENTATION GUIDE

> **Yêu cầu:** User đăng nhập bằng Google PHẢI cập nhật và verify số điện thoại trước khi mua hàng

---

## 📋 MỤC LỤC

1. [Workflow Tổng Quan](#1-workflow-tổng-quan)
2. [Database Changes](#2-database-changes)
3. [Backend Implementation](#3-backend-implementation)
4. [Frontend Implementation](#4-frontend-implementation)
5. [Testing Scenarios](#5-testing-scenarios)

---

## 1. WORKFLOW TỔNG QUAN

### 🔄 Luồng xử lý User OAuth2:

```
1. User login bằng Google
   ↓
2. CustomOAuth2UserService tạo/update user
   - provider = 'GOOGLE'
   - email_verified = true (tự động)
   - phone = null
   - phone_verified = false
   ↓
3. User browse products, add to cart → OK
   ↓
4. User click "Checkout"
   ↓
5. Backend kiểm tra: phone_verified == false?
   ↓
   YES → Redirect về /user/profile?requirePhone=true
   NO  → Cho phép checkout
   ↓
6. User cập nhật phone number + click "Verify Phone"
   ↓
7. Backend gửi OTP qua SMS (hoặc verify ngay)
   ↓
8. User nhập OTP
   ↓
9. Backend verify → Set phone_verified = true
   ↓
10. Redirect về /checkout → Cho phép đặt hàng
```

### 📊 So sánh User Types:

| User Type | Email Verified | Phone Verified | Checkout Allowed? |
|-----------|----------------|----------------|-------------------|
| LOCAL (register) | ✅ (sau verify email) | ✅ (phone bắt buộc khi đăng ký) | ✅ YES |
| GOOGLE OAuth2 | ✅ (auto) | ❌ (chưa có phone) | ❌ NO → Yêu cầu phone |
| OAuth2 + Updated Phone | ✅ (auto) | ✅ (sau verify) | ✅ YES |

---

## 2. DATABASE CHANGES

### ✅ Đã thêm vào schema:

```sql
ALTER TABLE users ADD phone_verified BIT DEFAULT 0;
```

### 📊 Logic update:

```sql
-- Default cho tất cả users
UPDATE users 
SET phone_verified = CASE 
    WHEN provider = 'LOCAL' AND phone IS NOT NULL AND LEN(phone) >= 10 THEN 1
    WHEN provider = 'GOOGLE' THEN 0
    ELSE 0
END;
```

---

## 3. BACKEND IMPLEMENTATION

### 📝 Bước 1: Update User Entity

**File:** `User.java`

```java
@Column(name = "phone_verified")
private Boolean phoneVerified = false;
```

✅ **ĐÃ CÓ** trong entity sau khi chạy migration.

---

### 📝 Bước 2: Update CustomOAuth2UserService

**File:** `CustomOAuth2UserService.java`

Thêm logic set `phone_verified = false` cho OAuth2 users:

```java
@Transactional
private User processOAuthUser(String email, String name, String provider, String providerId, String avatarUrl) {
    Optional<User> existingUser = userRepository.findByEmail(email);
    
    if (existingUser.isPresent()) {
        User user = existingUser.get();
        // ... existing code ...
        
        // OAuth2 users chưa verify phone
        if (user.getPhone() == null || user.getPhone().isEmpty()) {
            user.setPhoneVerified(false);
        }
        
        return userRepository.save(user);
    } else {
        User newUser = new User();
        // ... existing code ...
        newUser.setEmailVerified(true);
        newUser.setPhoneVerified(false);  // ← QUAN TRỌNG
        // ... save user ...
    }
}
```

---

### 📝 Bước 3: Tạo PhoneVerificationDTO

**File:** `PhoneVerificationDTO.java`

```java
package boiz.shop._2BShop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PhoneVerificationDTO {
    
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|84)(3|5|7|8|9)[0-9]{8}$", 
             message = "Số điện thoại không hợp lệ (VD: 0912345678)")
    private String phone;
    
    private String otp; // Optional - for OTP verification
}
```

---

### 📝 Bước 4: Update CheckoutController - Add Phone Verification Check

**File:** `CheckoutController.java`

Thêm validation trước khi cho checkout:

```java
@Controller
public class CheckoutController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/checkout")
    public String checkoutPage(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        
        if (principal == null) {
            return "redirect:/login";
        }
        
        String email = principal.getName();
        User user = userService.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // ========== KIỂM TRA PHONE VERIFICATION ==========
        if (!user.getPhoneVerified() || user.getPhone() == null || user.getPhone().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", 
                "Vui lòng cập nhật và xác thực số điện thoại để tiếp tục đặt hàng");
            redirectAttributes.addFlashAttribute("requirePhone", true);
            return "redirect:/user/profile";
        }
        // =================================================
        
        // ... rest of checkout logic ...
        
        return "user/checkout";
    }
}
```

---

### 📝 Bước 5: Tạo Phone Verification Service

**File:** `PhoneVerificationService.java`

```java
package boiz.shop._2BShop.service;

import boiz.shop._2BShop.entity.User;
import boiz.shop._2BShop.respository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class PhoneVerificationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailService mailService; // Hoặc SMSService nếu có

    private static final int OTP_EXPIRY_MINUTES = 5;

    public String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6-digit OTP
        return String.valueOf(otp);
    }

    @Transactional
    public void sendOTP(User user, String phone) {
        String otp = generateOTP();
        
        // Store OTP in session hoặc Redis (tạm thời dùng email)
        // TODO: Implement proper OTP storage
        
        // Send via SMS (tạm thời gửi qua email)
        String subject = "Mã xác thực số điện thoại - 2BShop";
        String message = String.format(
            "Xin chào %s,\n\n" +
            "Mã OTP của bạn là: %s\n" +
            "Mã này có hiệu lực trong %d phút.\n\n" +
            "Số điện thoại: %s\n\n" +
            "Trân trọng,\n2BShop Team",
            user.getFullName(), otp, OTP_EXPIRY_MINUTES, phone
        );
        
        mailService.sendSimpleMessage(user.getEmail(), subject, message);
    }

    @Transactional
    public boolean verifyPhoneAndUpdate(User user, String phone, String otp) {
        // TODO: Verify OTP from storage
        
        // Tạm thời: Accept bất kỳ phone nào (skip OTP)
        // Production: Phải verify OTP
        
        user.setPhone(phone);
        user.setPhoneVerified(true);
        user.setUpdatedDate(LocalDateTime.now());
        userRepository.save(user);
        
        return true;
    }

    @Transactional
    public void updatePhoneWithoutOTP(User user, String phone) {
        // For development/testing: Update phone without OTP
        user.setPhone(phone);
        user.setPhoneVerified(true);
        user.setUpdatedDate(LocalDateTime.now());
        userRepository.save(user);
    }
}
```

---

### 📝 Bước 6: Update UserProfileController

**File:** `UserProfileController.java`

Thêm endpoint để verify phone:

```java
@Controller
@RequestMapping("/user/profile")
public class UserProfileController {
    
    @Autowired
    private PhoneVerificationService phoneVerificationService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public String profile(Model model, Principal principal,
                         @RequestParam(required = false) Boolean requirePhone) {
        
        String email = principal.getName();
        User user = userService.findByEmail(email).orElseThrow();
        
        model.addAttribute("user", user);
        model.addAttribute("requirePhone", requirePhone != null && requirePhone);
        
        return "user/profile";
    }
    
    @PostMapping("/update-phone")
    public String updatePhone(@RequestParam String phone,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        try {
            String email = principal.getName();
            User user = userService.findByEmail(email).orElseThrow();
            
            // Validate phone format
            if (!phone.matches("^(0|84)(3|5|7|8|9)[0-9]{8}$")) {
                redirectAttributes.addFlashAttribute("error", 
                    "Số điện thoại không hợp lệ");
                return "redirect:/user/profile";
            }
            
            // Option 1: Send OTP (production)
            // phoneVerificationService.sendOTP(user, phone);
            // redirectAttributes.addFlashAttribute("success", 
            //     "Mã OTP đã được gửi đến email của bạn");
            
            // Option 2: Direct update (development/testing)
            phoneVerificationService.updatePhoneWithoutOTP(user, phone);
            redirectAttributes.addFlashAttribute("success", 
                "Cập nhật số điện thoại thành công!");
            
            return "redirect:/checkout";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/user/profile";
        }
    }
    
    @PostMapping("/verify-phone-otp")
    public String verifyPhoneOTP(@RequestParam String phone,
                                 @RequestParam String otp,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            String email = principal.getName();
            User user = userService.findByEmail(email).orElseThrow();
            
            boolean verified = phoneVerificationService.verifyPhoneAndUpdate(user, phone, otp);
            
            if (verified) {
                redirectAttributes.addFlashAttribute("success", 
                    "Xác thực số điện thoại thành công!");
                return "redirect:/checkout";
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "Mã OTP không chính xác hoặc đã hết hạn");
                return "redirect:/user/profile";
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/user/profile";
        }
    }
}
```

---

## 4. FRONTEND IMPLEMENTATION

### 🎨 Update profile.html

**File:** `templates/user/profile.html`

Thêm section Phone Verification:

```html
<!-- Phone Verification Alert (show when requirePhone=true) -->
<div th:if="${requirePhone}" class="alert alert-warning" role="alert">
    <i class="fas fa-exclamation-triangle"></i>
    <strong>Yêu cầu xác thực số điện thoại!</strong>
    <p>Để đảm bảo giao hàng được chính xác, vui lòng cập nhật số điện thoại của bạn.</p>
</div>

<!-- Existing profile tabs... -->

<!-- Phone Verification Tab Content -->
<div id="phone-verification" class="tab-content" th:classappend="${requirePhone} ? 'active' : ''">
    <h3>Cập nhật số điện thoại</h3>
    
    <div class="phone-status" th:if="${user.phoneVerified}">
        <i class="fas fa-check-circle text-success"></i>
        <span>Số điện thoại đã được xác thực: </span>
        <strong th:text="${user.phone}"></strong>
    </div>
    
    <form th:action="@{/user/profile/update-phone}" method="post" class="phone-form">
        <div class="form-group">
            <label for="phone">Số điện thoại <span class="required">*</span></label>
            <input type="text" 
                   id="phone" 
                   name="phone" 
                   class="form-control" 
                   placeholder="VD: 0912345678"
                   th:value="${user.phone}"
                   pattern="^(0|84)(3|5|7|8|9)[0-9]{8}$"
                   required />
            <small class="form-text text-muted">
                Định dạng: 0XXXXXXXXX (10 số, đầu số: 03, 05, 07, 08, 09)
            </small>
        </div>
        
        <!-- Option 1: Direct Update (Development) -->
        <button type="submit" class="btn btn-primary">
            <i class="fas fa-check"></i> Cập nhật số điện thoại
        </button>
        
        <!-- Option 2: OTP Verification (Production) -->
        <!--
        <button type="button" onclick="sendOTP()" class="btn btn-info">
            <i class="fas fa-envelope"></i> Gửi mã OTP
        </button>
        
        <div id="otp-section" style="display: none;">
            <div class="form-group">
                <label for="otp">Mã OTP</label>
                <input type="text" id="otp" name="otp" class="form-control" 
                       placeholder="Nhập mã 6 số" maxlength="6" />
            </div>
            <button type="submit" formaction="/user/profile/verify-phone-otp" class="btn btn-success">
                <i class="fas fa-check-circle"></i> Xác thực OTP
            </button>
        </div>
        -->
    </form>
</div>
```

### 📱 CSS Styling:

```css
.phone-status {
    background-color: #d4edda;
    border: 1px solid #c3e6cb;
    border-radius: 5px;
    padding: 15px;
    margin-bottom: 20px;
}

.phone-status i {
    font-size: 20px;
    margin-right: 10px;
}

.phone-form {
    max-width: 500px;
}

.alert-warning {
    border-left: 4px solid #ffc107;
}
```

---

### 🎨 Update checkout.html

Hiển thị phone number trong checkout form:

```html
<div class="checkout-info">
    <h3>Thông tin giao hàng</h3>
    
    <div class="info-row">
        <label>Người nhận:</label>
        <span th:text="${user.fullName}">Nguyễn Văn A</span>
    </div>
    
    <div class="info-row">
        <label>Số điện thoại:</label>
        <span th:text="${user.phone}">0912345678</span>
        <span th:if="${user.phoneVerified}" class="badge badge-success">
            <i class="fas fa-check"></i> Đã xác thực
        </span>
    </div>
    
    <!-- ... rest of checkout form ... -->
</div>
```

---

## 5. TESTING SCENARIOS

### ✅ Test Case 1: Local User (Không cần verify phone)

**Bước:**
1. Register bằng email/password → Nhập phone trong form
2. Verify email
3. Login
4. Checkout → ✅ Cho phép ngay

**Expected:** `phone_verified = 1` (set tự động khi register)

---

### ✅ Test Case 2: Google OAuth2 User (Chưa có phone)

**Bước:**
1. Login bằng Google → phone = null, phone_verified = 0
2. Add sản phẩm vào cart
3. Click "Checkout"
4. **Expected:** Redirect về `/user/profile?requirePhone=true`
5. Hiển thị warning: "Vui lòng cập nhật số điện thoại"
6. User nhập phone → Submit
7. Backend set `phone_verified = 1`
8. Redirect về `/checkout` → ✅ Cho phép đặt hàng

---

### ✅ Test Case 4: OAuth2 User đã update phone

**Bước:**
1. Login bằng Google
2. Previous session đã update phone → `phone_verified = 1`
3. Click "Checkout"
4. **Expected:** Cho phép checkout ngay, không redirect

---

### 🧪 SQL Queries để test:

```sql
-- Check user phone verification status
SELECT 
    user_id,
    username,
    email,
    provider,
    phone,
    phone_verified,
    is_enabled
FROM users
ORDER BY created_date DESC;

-- Find OAuth2 users without verified phone
SELECT * FROM users 
WHERE provider IN ('GOOGLE', 'FACEBOOK') 
  AND (phone_verified = 0 OR phone IS NULL);

-- Update specific user's phone verification
UPDATE users 
SET phone = '0912345678', phone_verified = 1 
WHERE user_id = 5;
```

---

## 6. PRODUCTION ENHANCEMENTS

### 🔒 Bảo mật:

1. **Rate Limiting:** Giới hạn số lần gửi OTP (max 3 lần/15 phút)
2. **OTP Expiry:** OTP chỉ hiệu lực 5 phút
3. **OTP Storage:** Dùng Redis hoặc database table riêng
4. **Phone Format Validation:** Server-side + client-side
5. **Prevent Duplicate Phone:** Check phone đã được dùng bởi user khác chưa

### 📱 SMS Integration:

```java
// Sử dụng Twilio, Vonage, hoặc SMS gateway Việt Nam:
// - VNPT SMS
// - Viettel SMS
// - Esms.vn
// - Speedsms.vn

@Service
public class SmsService {
    
    public void sendOTP(String phone, String otp) {
        // Integration code here
        // Example with Twilio:
        // Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        // Message.creator(
        //     new PhoneNumber(phone),
        //     new PhoneNumber(FROM_NUMBER),
        //     "Your OTP is: " + otp
        // ).create();
    }
}
```

---

## 7. ROLLBACK PLAN

Nếu cần disable phone verification tạm thời:

```java
// In CheckoutController.java
// Comment out phone verification check:

/*
if (!user.getPhoneVerified() || user.getPhone() == null) {
    return "redirect:/user/profile?requirePhone=true";
}
*/
```

Hoặc update tất cả OAuth2 users:

```sql
UPDATE users 
SET phone_verified = 1 
WHERE provider = 'GOOGLE';
```

---

## 📊 SUMMARY CHECKLIST

- [x] Database: Thêm `phone_verified` column
- [x] Entity: Update User.java
- [x] Service: CustomOAuth2UserService set phoneVerified = false
- [x] Service: Tạo PhoneVerificationService
- [x] Controller: CheckoutController validation
- [x] Controller: UserProfileController phone update endpoints
- [x] Frontend: Profile page phone form
- [x] Frontend: Checkout page display phone status
- [x] Testing: Test all scenarios
- [ ] Production: Integrate SMS gateway
- [ ] Production: Implement OTP storage (Redis)
- [ ] Production: Add rate limiting

---

**📝 Ghi chú:** Document này là phần bổ sung cho OAuth2 setup. Xem thêm `OAUTH2_SETUP_GUIDE.md` để biết chi tiết về Google login configuration.
