package boiz.shop._2BShop.api.controller.auth;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import boiz.shop._2BShop.api.dto.AuthUserDto;
import boiz.shop._2BShop.api.mapper.ApiMapper;
import boiz.shop._2BShop.api.request.ConfirmRegisterRequest;
import boiz.shop._2BShop.api.request.EmailRequest;
import boiz.shop._2BShop.api.response.ApiResponse;
import boiz.shop._2BShop.api.util.CurrentUserService;
import boiz.shop._2BShop.dto.RegisterDTO;
import boiz.shop._2BShop.dto.ResetPasswordDTO;
import boiz.shop._2BShop.entity.User;
import boiz.shop._2BShop.service.PasswordResetService;
import boiz.shop._2BShop.service.UserService;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthApiController {

    private final CurrentUserService currentUserService;
    private final ApiMapper apiMapper;
    private final UserService userService;
    private final PasswordResetService passwordResetService;

    public AuthApiController(
            CurrentUserService currentUserService,
            ApiMapper apiMapper,
            UserService userService,
            PasswordResetService passwordResetService) {
        this.currentUserService = currentUserService;
        this.apiMapper = apiMapper;
        this.userService = userService;
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/me")
    public ApiResponse<AuthUserDto> me(Principal principal) {
        String email = currentUserService.extractEmail(principal);
        User user = email == null ? null : userService.findByEmail(email).orElse(null);
        return ApiResponse.success(apiMapper.toAuthUserDto(user, user != null));
    }

    @GetMapping("/csrf")
    public ApiResponse<Map<String, String>> csrf(CsrfToken csrfToken) {
        Map<String, String> token = new LinkedHashMap<>();
        token.put("headerName", csrfToken.getHeaderName());
        token.put("parameterName", csrfToken.getParameterName());
        token.put("token", csrfToken.getToken());
        return ApiResponse.success(token);
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(@RequestBody RegisterDTO dto) {
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new RuntimeException("Email không được để trống");
        }
        if (dto.getUserName() == null || dto.getUserName().isBlank()) {
            throw new RuntimeException("Tên đăng nhập không được để trống");
        }
        if (dto.getFullName() == null || dto.getFullName().isBlank()) {
            throw new RuntimeException("Họ tên không được để trống");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            throw new RuntimeException("Mật khẩu phải có ít nhất 6 ký tự");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp");
        }
        userService.registerUser(dto);
        return ApiResponse.success("Đăng ký thành công. Vui lòng kiểm tra email để xác thực tài khoản.", null);
    }

    @PostMapping("/confirm-register")
    public ApiResponse<Void> confirmRegister(@RequestBody ConfirmRegisterRequest request) {
        boolean valid = userService.verifyEmailToken(request.email(), request.token());
        if (!valid) {
            throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn");
        }
        userService.activateUser(request.email());
        return ApiResponse.success("Xác thực email thành công. Bạn có thể đăng nhập ngay bây giờ.", null);
    }

    @PostMapping("/resend-verification")
    public ApiResponse<Void> resendVerification(@RequestBody EmailRequest request) {
        userService.resendVerificationEmail(request.email());
        return ApiResponse.success("Email xác thực đã được gửi lại.", null);
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@RequestBody EmailRequest request) {
        passwordResetService.createResetToken(request.email());
        return ApiResponse.success(
                "Nếu email tồn tại trong hệ thống, bạn sẽ nhận được liên kết đặt lại mật khẩu trong ít phút.",
                null);
    }

    @GetMapping("/reset-password/validate")
    public ApiResponse<Map<String, Boolean>> validateResetToken(@RequestParam("token") String token) {
        return ApiResponse.success(Map.of("valid", passwordResetService.validateToken(token)));
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        passwordResetService.resetPassword(dto);
        return ApiResponse.success("Đổi mật khẩu thành công. Vui lòng đăng nhập với mật khẩu mới.", null);
    }
}
