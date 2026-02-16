package boiz.shop._2BShop.controller;

import boiz.shop._2BShop.dto.ResetPasswordDTO;
import boiz.shop._2BShop.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    /**
     * Show forgot password form
     */
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "auth/forgot-password";
    }

    /**
     * Process forgot password request
     */
    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {

        try {
            passwordResetService.createResetToken(email);
            redirectAttributes.addFlashAttribute("success",
                    "Nếu email tồn tại trong hệ thống, bạn sẽ nhận được link khôi phục mật khẩu trong vài phút.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra. Vui lòng thử lại.");
        }

        return "redirect:/forgot-password";
    }

    /**
     * Show reset password form
     */
    @GetMapping("/reset-password")
    public String showResetPasswordForm(
            @RequestParam("token") String token,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Validate token
        if (!passwordResetService.validateToken(token)) {
            redirectAttributes.addFlashAttribute("error",
                    "Link khôi phục mật khẩu không hợp lệ hoặc đã hết hạn.");
            return "redirect:/forgot-password";
        }

        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setToken(token);
        model.addAttribute("resetPasswordDTO", dto);

        return "auth/reset-password";
    }

    /**
     * Process password reset
     */
    @PostMapping("/reset-password")
    public String processResetPassword(
            @Valid @ModelAttribute("resetPasswordDTO") ResetPasswordDTO dto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng kiểm tra lại thông tin");
            return "redirect:/reset-password?token=" + dto.getToken();
        }

        try {
            passwordResetService.resetPassword(dto);
            redirectAttributes.addFlashAttribute("success",
                    "Đổi mật khẩu thành công. Vui lòng đăng nhập với mật khẩu mới.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reset-password?token=" + dto.getToken();
        }
    }
}
