package boiz.shop._2BShop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Account Controller - Xử lý các URL /account/*
 * Redirect các URL cũ sang URL mới để tương thích
 */
@Controller
@RequestMapping("/account")
public class AccountController {

    /**
     * /account → redirect về /profile
     */
    @GetMapping
    public String account() {
        return "redirect:/profile";
    }

    /**
     * /account/orders → redirect về /orders
     */
    @GetMapping("/orders")
    public String orders() {
        return "redirect:/orders";
    }

    /**
     * /account/change-password → redirect về /profile (có form đổi mật khẩu)
     */
    @GetMapping("/change-password")
    public String changePassword() {
        return "redirect:/profile";
    }
}
