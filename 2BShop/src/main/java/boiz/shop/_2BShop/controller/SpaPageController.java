package boiz.shop._2BShop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaPageController {

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
            "/admin/bank-accounts/{id}/edit"
    })
    public String forwardSpa() {
        return "forward:/index.html";
    }
}
