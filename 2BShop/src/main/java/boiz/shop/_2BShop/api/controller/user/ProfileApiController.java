package boiz.shop._2BShop.api.controller.user;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import boiz.shop._2BShop.api.dto.AuthUserDto;
import boiz.shop._2BShop.api.mapper.ApiMapper;
import boiz.shop._2BShop.api.request.ChangePasswordRequest;
import boiz.shop._2BShop.api.request.PhoneUpdateRequest;
import boiz.shop._2BShop.api.request.ProfileUpdateRequest;
import boiz.shop._2BShop.api.response.ApiResponse;
import boiz.shop._2BShop.api.util.CurrentUserService;
import boiz.shop._2BShop.dto.ChangePasswordDTO;
import boiz.shop._2BShop.dto.UserProfileDTO;
import boiz.shop._2BShop.entity.User;
import boiz.shop._2BShop.service.PhoneVerificationService;
import boiz.shop._2BShop.service.UserProfileService;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileApiController {

    private final CurrentUserService currentUserService;
    private final UserProfileService userProfileService;
    private final PhoneVerificationService phoneVerificationService;
    private final ApiMapper apiMapper;

    public ProfileApiController(
            CurrentUserService currentUserService,
            UserProfileService userProfileService,
            PhoneVerificationService phoneVerificationService,
            ApiMapper apiMapper) {
        this.currentUserService = currentUserService;
        this.userProfileService = userProfileService;
        this.phoneVerificationService = phoneVerificationService;
        this.apiMapper = apiMapper;
    }

    @GetMapping("/me")
    public ApiResponse<AuthUserDto> me() {
        User user = currentUserService.getCurrentUserOrThrow();
        return ApiResponse.success(apiMapper.toAuthUserDto(user, true));
    }

    @PutMapping
    public ApiResponse<AuthUserDto> updateProfile(@RequestBody ProfileUpdateRequest request) {
        User user = currentUserService.getCurrentUserOrThrow();
        UserProfileDTO dto = userProfileService.getUserProfile(user.getUserId());
        dto.setFullName(request.fullName());
        dto.setPhone(request.phone());
        dto.setAddress(request.address());
        userProfileService.updateProfile(dto);
        return ApiResponse.success("Cập nhật thông tin thành công", apiMapper.toAuthUserDto(currentUserService.getCurrentUserOrThrow(), true));
    }

    @PostMapping("/avatar")
    public ApiResponse<Map<String, String>> uploadAvatar(@RequestParam("avatar") MultipartFile avatar) throws Exception {
        User user = currentUserService.getCurrentUserOrThrow();
        String avatarUrl = userProfileService.updateAvatar(user.getUserId(), avatar);
        return ApiResponse.success("Cập nhật ảnh đại diện thành công", Map.of("avatarUrl", avatarUrl));
    }

    @PostMapping("/phone")
    public ApiResponse<AuthUserDto> updatePhone(@RequestBody PhoneUpdateRequest request) {
        User user = currentUserService.getCurrentUserOrThrow();
        phoneVerificationService.updatePhoneAndVerify(user, request.phone());
        return ApiResponse.success("Cập nhật số điện thoại thành công", apiMapper.toAuthUserDto(currentUserService.getCurrentUserOrThrow(), true));
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        User user = currentUserService.getCurrentUserOrThrow();
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setUserId(user.getUserId());
        dto.setCurrentPassword(request.currentPassword());
        dto.setNewPassword(request.newPassword());
        dto.setConfirmPassword(request.confirmPassword());
        userProfileService.changePassword(dto);
        return ApiResponse.success("Đổi mật khẩu thành công", null);
    }
}
