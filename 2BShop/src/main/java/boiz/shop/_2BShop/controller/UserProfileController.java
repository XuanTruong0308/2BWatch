package boiz.shop._2BShop.controller;

import boiz.shop._2BShop.dto.ChangePasswordDTO;
import boiz.shop._2BShop.dto.UserProfileDTO;
import boiz.shop._2BShop.entity.User;
import boiz.shop._2BShop.service.PhoneVerificationService;
import boiz.shop._2BShop.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/profile")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private boiz.shop._2BShop.respository.UserRepository userRepository;
    
    @Autowired
    private PhoneVerificationService phoneVerificationService;

    /**
     * Helper method to extract email from Principal (supports both UserDetails and OAuth2User)
     */
    private String getEmailFromPrincipal(Principal principal) {
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            // Try to get email from CustomOAuth2User
            Object emailAttr = oauth2User.getAttribute("email");
            if (emailAttr != null) {
                return emailAttr.toString();
            }
            // Fallback to name
            return oauth2User.getName();
        }
        return principal.getName();
    }

    /**
     * Show profile page
     */
    @GetMapping
    public String showProfile(Principal principal, Model model) {
        System.out.println("=== DEBUG: /profile endpoint accessed");
        System.out.println("  - Principal: " + (principal != null ? principal.getName() : "NULL"));
        System.out.println("  - Principal type: " + (principal != null ? principal.getClass().getName() : "NULL"));

        if (principal == null) {
            return "redirect:/login";
        }

        String email = getEmailFromPrincipal(principal);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        UserProfileDTO profileDTO = userProfileService.getUserProfile(user.getUserId());
        model.addAttribute("profile", profileDTO);
        model.addAttribute("changePasswordDTO", new ChangePasswordDTO());
        
        // Check if requirePhone flash attribute exists (from checkout redirect)
        if (!model.containsAttribute("requirePhone")) {
            model.addAttribute("requirePhone", false);
        }

        return "user/profile";
    }

    /**
     * Update profile
     */
    @PostMapping("/update")
    public String updateProfile(
            @Valid @ModelAttribute("profile") UserProfileDTO profileDTO,
            BindingResult result,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng kiểm tra lại thông tin");
            return "redirect:/profile";
        }

        try {
            String email = getEmailFromPrincipal(principal);
            User user = userRepository.findByEmail(email)
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
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            String email = getEmailFromPrincipal(principal);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            userProfileService.updateAvatar(user.getUserId(), file);
            redirectAttributes.addFlashAttribute("success", "Cập nhật ảnh đại diện thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi upload ảnh: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    /**
     * Update phone number (for OAuth2 users)
     */
    @PostMapping("/update-phone")
    public String updatePhone(
            @RequestParam String phone,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            String email = getEmailFromPrincipal(principal);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            phoneVerificationService.updatePhoneAndVerify(user, phone);
            
            redirectAttributes.addFlashAttribute("success", 
                "Cập nhật số điện thoại thành công! Bạn có thể đặt hàng ngay.");
            
            // Redirect to checkout if came from checkout page
            if (phoneVerificationService.needsPhoneVerification(user)) {
                return "redirect:/profile";
            }
            return "redirect:/checkout";
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/profile";
        }
    }

    /**
     * Change password (AJAX endpoint)
     */
    @PostMapping("/change-password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> changePassword(
            @Valid @RequestBody ChangePasswordDTO changePasswordDTO,
            Principal principal) {

        Map<String, Object> response = new HashMap<>();

        try {
            String email = getEmailFromPrincipal(principal);
            User user = userRepository.findByEmail(email)
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
