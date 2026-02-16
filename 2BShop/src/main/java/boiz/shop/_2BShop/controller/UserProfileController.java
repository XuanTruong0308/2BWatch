package boiz.shop._2BShop.controller;

import boiz.shop._2BShop.dto.ChangePasswordDTO;
import boiz.shop._2BShop.dto.UserProfileDTO;
import boiz.shop._2BShop.entity.User;
import boiz.shop._2BShop.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/profile")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private boiz.shop._2BShop.respository.UserRepository userRepository;

    /**
     * Show profile page
     */
    @GetMapping
    public String showProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        System.out.println("=== DEBUG: /profile endpoint accessed");
        System.out.println("  - User: " + (userDetails != null ? userDetails.getUsername() : "NULL"));
        if (userDetails != null) {
            System.out.println("  - Authorities: " + userDetails.getAuthorities());
        }

        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        UserProfileDTO profileDTO = userProfileService.getUserProfile(user.getUserId());
        model.addAttribute("profile", profileDTO);
        model.addAttribute("changePasswordDTO", new ChangePasswordDTO());

        return "user/profile";
    }

    /**
     * Update profile
     */
    @PostMapping("/update")
    public String updateProfile(
            @Valid @ModelAttribute("profile") UserProfileDTO profileDTO,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng kiểm tra lại thông tin");
            return "redirect:/profile";
        }

        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            profileDTO.setUserId(user.getUserId());
            userProfileService.updateProfile(profileDTO);

            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    /**
     * Upload avatar
     */
    @PostMapping("/upload-avatar")
    public String uploadAvatar(
            @RequestParam("avatar") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            userProfileService.updateAvatar(user.getUserId(), file);
            redirectAttributes.addFlashAttribute("success", "Cập nhật ảnh đại diện thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi upload ảnh: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    /**
     * Change password (AJAX endpoint)
     */
    @PostMapping("/change-password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> changePassword(
            @Valid @RequestBody ChangePasswordDTO changePasswordDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();

        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            changePasswordDTO.setUserId(user.getUserId());
            userProfileService.changePassword(changePasswordDTO);

            response.put("success", true);
            response.put("message", "Đổi mật khẩu thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
