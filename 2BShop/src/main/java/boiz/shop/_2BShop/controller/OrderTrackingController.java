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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/my-orders")
public class OrderTrackingController {

    @Autowired
    private OrderTrackingService orderTrackingService;

    @Autowired
    private UserRepository userRepository;

    /**
     * List user's orders with pagination
     */
    @GetMapping
    public String listOrders(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) String status,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User user = userRepository.findByEmail(userDetails.getUsername())
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
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User user = userRepository.findByEmail(userDetails.getUsername())
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
