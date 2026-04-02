package boiz.shop._2BShop.controller.api;

import boiz.shop._2BShop.dto.ChangePasswordDTO;
import boiz.shop._2BShop.dto.UserProfileDTO;
import boiz.shop._2BShop.entity.User;
import boiz.shop._2BShop.respository.UserRepository;
import boiz.shop._2BShop.service.PhoneVerificationService;
import boiz.shop._2BShop.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Profile API")
@RestController
@RequestMapping("/api/v1/profile")
public class ProfileApiController {
    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PhoneVerificationService phoneVerificationService;

    @Operation(summary = "Get current user profile")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getProfile(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = ApiAuthSupport.extractEmail(principal);
            if (email == null) {
                response.put("success", false);
                response.put("message", "Bạn cần đăng nhập!");
                return ResponseEntity.status(401).body(response);
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            UserProfileDTO profileDTO = userProfileService.getUserProfile(user.getUserId());
            response.put("success", true);
            response.put("data", profileDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Update current user profile")
    @PutMapping
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestBody UserProfileDTO profileDTO,
            Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = ApiAuthSupport.extractEmail(principal);
            if (email == null) {
                response.put("success", false);
                response.put("message", "Bạn cần đăng nhập!");
                return ResponseEntity.status(401).body(response);
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            profileDTO.setUserId(user.getUserId());
            UserProfileDTO updated = userProfileService.updateProfile(profileDTO);

            response.put("success", true);
            response.put("message", "Cập nhật thông tin thành công");
            response.put("data", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Upload avatar")
    @PostMapping("/upload-avatar")
    public ResponseEntity<Map<String, Object>> uploadAvatar(
            @RequestParam("avatar") MultipartFile file,
            Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = ApiAuthSupport.extractEmail(principal);
            if (email == null) {
                response.put("success", false);
                response.put("message", "Bạn cần đăng nhập!");
                return ResponseEntity.status(401).body(response);
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            String avatarUrl = userProfileService.updateAvatar(user.getUserId(), file);

            response.put("success", true);
            response.put("message", "Cập nhật ảnh đại diện thành công");
            response.put("avatarUrl", avatarUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi upload ảnh: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Update phone number")
    @PostMapping("/update-phone")
    public ResponseEntity<Map<String, Object>> updatePhone(
            @RequestBody Map<String, String> body,
            Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = ApiAuthSupport.extractEmail(principal);
            if (email == null) {
                response.put("success", false);
                response.put("message", "Bạn cần đăng nhập!");
                return ResponseEntity.status(401).body(response);
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            phoneVerificationService.updatePhoneAndVerify(user, body.get("phone"));
            boolean stillNeedPhone = phoneVerificationService.needsPhoneVerification(user);

            response.put("success", true);
            response.put("message", "Cập nhật số điện thoại thành công! Bạn có thể đặt hàng ngay.");
            response.put("next", stillNeedPhone ? "profile" : "checkout");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Change current user password")
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestBody ChangePasswordDTO dto,
            Principal principal) {
        Map<String, Object> res = new HashMap<>();
        try {
            if (principal == null) {
                res.put("success", false);
                res.put("message", "Bạn cần đăng nhập!");
                return ResponseEntity.badRequest().body(res);
            }

            var user = userRepository.findByEmail(ApiAuthSupport.extractEmail(principal))
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
            dto.setUserId(user.getUserId());
            userProfileService.changePassword(dto);
            res.put("success", true);
            res.put("message", "Đổi mật khẩu thành công");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }
}