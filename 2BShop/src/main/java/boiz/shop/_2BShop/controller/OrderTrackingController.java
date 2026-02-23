package boiz.shop._2BShop.controller;

import boiz.shop._2BShop.entity.Order;
import boiz.shop._2BShop.entity.User;
import boiz.shop._2BShop.respository.UserRepository;
import boiz.shop._2BShop.service.OrderTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/my-orders")
public class OrderTrackingController {

    @Autowired
    private OrderTrackingService orderTrackingService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Helper method to extract email from Principal
     */
    private String getEmailFromPrincipal(Principal principal) {
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            Object emailAttr = oauth2User.getAttribute("email");
            if (emailAttr != null) {
                return emailAttr.toString();
            }
            return oauth2User.getName();
        }
        return principal.getName();
    }

    /**
     * List user's orders with pagination
     */
    @GetMapping
    public String listOrders(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) String status,
            Principal principal,
            Model model) {

        String email = getEmailFromPrincipal(principal);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders;

        if (status != null && !status.isEmpty()) {
            orders = orderTrackingService.getUserOrdersByStatus(user.getUserId(), status, pageable);
            model.addAttribute("selectedStatus", status);
        } else {
            orders = orderTrackingService.getUserOrders(user.getUserId(), pageable);
        }

        model.addAttribute("orders", orders);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());

        return "user/my-orders";
    }

    /**
     * View order details
     */
    @GetMapping("/{orderId}")
    public String viewOrderDetails(
            @PathVariable Integer orderId,
            Principal principal,
            Model model) {

        String email = getEmailFromPrincipal(principal);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        try {
            Order order = orderTrackingService.getOrderDetails(orderId, user.getUserId());
            model.addAttribute("order", order);
            return "user/order-detail";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/my-orders";
        }
    }
}
