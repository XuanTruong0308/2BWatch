package boiz.shop._2BShop.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import boiz.shop._2BShop.service.CartService;

/**
 * Global Controller Advice
 * Tự động thêm các attributes chung cho tất cả các views
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private CartService cartService;

    /**
     * Tự động thêm cartItemCount vào mọi view
     * Hiển thị badge số lượng sản phẩm trong giỏ hàng trên header
     */
    @ModelAttribute
    public void addCartItemCount(Model model) {
        try {
            // Kiểm tra user đã login chưa
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                int count = cartService.getCartItemCount();
                model.addAttribute("cartItemCount", count);
            } else {
                model.addAttribute("cartItemCount", 0);
            }
        } catch (Exception e) {
            // Nếu có lỗi, set count = 0 để tránh crash view
            model.addAttribute("cartItemCount", 0);
        }
    }
}
