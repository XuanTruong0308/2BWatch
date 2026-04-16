package boiz.shop._2BShop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class SpaPageController {

    private static final String DEV_FRONTEND_ORIGIN = "http://localhost:5173";

    @GetMapping({
            "/",
            "/login",
            "/register",
            "/forgot-password",
            "/reset-password",
            "/confirm-register",
            "/about",
            "/contact",
            "/policy",
            "/terms",
            "/faq",
            "/cart",
            "/checkout",
            "/checkout/confirmation/{orderId}",
            "/payment-result",
            "/profile",
            "/my-orders",
            "/my-orders/{orderId}",
            "/watches",
            "/watches/newest",
            "/watches/discount",
            "/watches/{id}",
            "/products/{id}",
            "/user/cart",
            "/user/checkout",
            "/user/profile",
            "/user/orders",
            "/user/orders/{id}",
            "/account",
            "/account/orders",
            "/account/change-password",
            "/admin",
            "/admin/",
            "/admin/dashboard",
            "/admin/brands",
            "/admin/brands/new",
            "/admin/brands/{id}/edit",
            "/admin/watches",
            "/admin/watches/new",
            "/admin/watches/{id}/edit",
            "/admin/users",
            "/admin/users/new",
            "/admin/users/{id}",
            "/admin/users/{id}/edit",
            "/admin/orders",
            "/admin/orders/{id}",
            "/admin/payments/methods",
            "/admin/payments/methods/new",
            "/admin/payments/methods/{id}/edit",
            "/admin/payments/transactions",
            "/admin/payments/transactions/{id}",
            "/admin/bank-accounts",
            "/admin/bank-accounts/new",
            "/admin/bank-accounts/{id}/edit",
            "/admin/support-chat"
    })
    public String forwardSpa(HttpServletRequest request) {
        if (shouldRedirectToVite(request)) {
            String queryString = request.getQueryString();
            String target = DEV_FRONTEND_ORIGIN + request.getRequestURI();
            if (queryString != null && !queryString.isBlank()) {
                target += "?" + queryString;
            }
            return "redirect:" + target;
        }

        return "forward:/index.html";
    }

    private boolean shouldRedirectToVite(HttpServletRequest request) {
        String serverName = request.getServerName();
        boolean isLocalRequest = "localhost".equalsIgnoreCase(serverName) || "127.0.0.1".equals(serverName);
        return isLocalRequest && request.getServerPort() != 5173;
    }
}
