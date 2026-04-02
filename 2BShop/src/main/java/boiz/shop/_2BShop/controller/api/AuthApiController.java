package boiz.shop._2BShop.controller.api;

import boiz.shop._2BShop.dto.RegisterDTO;
import boiz.shop._2BShop.dto.ResetPasswordDTO;
import boiz.shop._2BShop.entity.User;
import boiz.shop._2BShop.service.PasswordResetService;
import boiz.shop._2BShop.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Auth API")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordResetService passwordResetService;

    @Operation(summary = "Register account")
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterDTO dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
                throw new RuntimeException("Email không được để trống!");
            }

            if (dto.getPassword() == null || dto.getPassword().length() < 6) {
                throw new RuntimeException("Mật khẩu phải có ít nhất 6 ký tự!");
            }

            if (!dto.getPassword().equals(dto.getConfirmPassword())) {
                throw new RuntimeException("Mật khẩu xác nhận không khớp!");
            }

            if (userService.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Email đã được sử dụng!");
            }

            userService.registerUser(dto);

            response.put("success", true);
            response.put("message", "Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Get current authentication state")
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        String email = ApiAuthSupport.extractEmail(principal);
        boolean authenticated = email != null && !email.isBlank();

        response.put("success", true);
        response.put("authenticated", authenticated);
        response.put("message", authenticated ? "Authenticated" : "Not authenticated");

        if (authenticated) {
            User user = userService.findByEmail(email).orElse(null);
            response.put("user", ApiDataMapper.userSummary(user));
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Confirm register")
    @PostMapping("/confirm-register")
    public ResponseEntity<Map<String, Object>> confirmRegister(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = ApiValueParser.asString(payload.get("email"));
            String token = ApiValueParser.asString(payload.get("token"));

            boolean isValid = userService.verifyEmailToken(email, token);
            if (!isValid) {
                throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn!");
            }

            userService.activateUser(email);

            response.put("success", true);
            response.put("message", "Xác thực email thành công! Bạn có thể đăng nhập ngay bây giờ.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Resend verification email")
    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, Object>> resendVerification(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = ApiValueParser.asString(payload.get("email"));

            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với email này!"));

            if (Boolean.TRUE.equals(user.getIsEnabled())) {
                throw new RuntimeException("Tài khoản đã được kích hoạt!");
            }

            userService.resendVerificationEmail(email);

            response.put("success", true);
            response.put("message", "Email xác thực đã được gửi lại! Vui lòng kiểm tra hộp thư.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Forgot password")
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = ApiValueParser.asString(payload.get("email"));
            passwordResetService.createResetToken(email);

            response.put("success", true);
            response.put("message", "Nếu email tồn tại trong hệ thống, bạn sẽ nhận được link khôi phục mật khẩu trong vài phút.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra. Vui lòng thử lại.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Reset password")
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            passwordResetService.resetPassword(dto);
            response.put("success", true);
            response.put("message", "Đổi mật khẩu thành công. Vui lòng đăng nhập với mật khẩu mới.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
