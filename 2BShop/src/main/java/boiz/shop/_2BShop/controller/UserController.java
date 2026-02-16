package boiz.shop._2BShop.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import boiz.shop._2BShop.entity.CartItem;
import boiz.shop._2BShop.entity.Order;
import boiz.shop._2BShop.entity.User;
import boiz.shop._2BShop.service.CartService;
import boiz.shop._2BShop.service.OrderService;
import boiz.shop._2BShop.service.UserService;

/**
 * ========================================
 * USER CONTROLLER - TẤT CẢ CHỨC NĂNG USER
 * ========================================
 * Bao gồm:
 * 1. Cart Management - Quản lý giỏ hàng
 * 2. Order Management - Quản lý đơn hàng của user
 * 3. Checkout - Thanh toán
 * 4. Profile - Quản lý thông tin cá nhân
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    // ========================================
    // 1. CART MANAGEMENT - QUẢN LÝ GIỎ HÀNG
    // ========================================

    /**
     * Xem giỏ hàng
     */
    @GetMapping("/cart")
    public String viewCart(Model model, Principal principal) {
        try {
            if (principal == null) {
                return "redirect:/login";
            }

            List<CartItem> cartItems = cartService.getCurrentUserCartItems();
            BigDecimal subtotal = cartService.calculateSubtotal();
            BigDecimal shippingFee = subtotal.compareTo(new BigDecimal("500000")) >= 0
                    ? BigDecimal.ZERO
                    : new BigDecimal("30000");
            BigDecimal total = subtotal.add(shippingFee);

            model.addAttribute("cartItems", cartItems);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("shippingFee", shippingFee);
            model.addAttribute("total", total);
            model.addAttribute("cartItemCount", cartItems.size());

        } catch (Exception e) {
            model.addAttribute("cartItems", List.of());
            model.addAttribute("subtotal", BigDecimal.ZERO);
            model.addAttribute("total", BigDecimal.ZERO);
            model.addAttribute("error", "Lỗi khi tải giỏ hàng: " + e.getMessage());
        }

        return "user/cart";
    }

    /**
     * Thêm sản phẩm vào giỏ hàng (AJAX)
     */
    @PostMapping("/cart/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestParam Integer watchId,
            @RequestParam(defaultValue = "1") Integer quantity,
            Principal principal) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (principal == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập để thêm vào giỏ hàng");
                return ResponseEntity.ok(response);
            }

            cartService.addToCart(watchId, quantity);

            // Lấy số lượng cart items mới
            int cartItemCount = cartService.getCartItemCount();

            response.put("success", true);
            response.put("message", "Đã thêm vào giỏ hàng!");
            response.put("cartItemCount", cartItemCount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ (AJAX)
     */
    @PostMapping("/cart/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCartItem(
            @RequestParam Integer cartItemId,
            @RequestParam Integer quantity) {
        Map<String, Object> response = new HashMap<>();

        try {
            cartService.updateQuantity(cartItemId, quantity);

            // Tính lại subtotal và total
            BigDecimal subtotal = cartService.calculateSubtotal();
            BigDecimal shippingFee = subtotal.compareTo(new BigDecimal("500000")) >= 0
                    ? BigDecimal.ZERO
                    : new BigDecimal("30000");
            BigDecimal total = subtotal.add(shippingFee);

            response.put("success", true);
            response.put("subtotal", subtotal);
            response.put("shippingFee", shippingFee);
            response.put("total", total);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng (AJAX)
     */
    @PostMapping("/cart/remove")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFromCart(
            @RequestParam Integer cartItemId) {
        Map<String, Object> response = new HashMap<>();

        try {
            cartService.removeCartItem(cartItemId);

            // Tính lại subtotal và total
            BigDecimal subtotal = cartService.calculateSubtotal();
            BigDecimal shippingFee = subtotal.compareTo(new BigDecimal("500000")) >= 0
                    ? BigDecimal.ZERO
                    : new BigDecimal("30000");
            BigDecimal total = subtotal.add(shippingFee);

            int cartItemCount = cartService.getCartItemCount();

            response.put("success", true);
            response.put("message", "Đã xóa khỏi giỏ hàng!");
            response.put("subtotal", subtotal);
            response.put("shippingFee", shippingFee);
            response.put("total", total);
            response.put("cartItemCount", cartItemCount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    @PostMapping("/cart/clear")
    public String clearCart(RedirectAttributes redirectAttributes) {
        try {
            cartService.clearCart();
            redirectAttributes.addFlashAttribute("success", "Đã xóa toàn bộ giỏ hàng!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/user/cart";
    }

    // ========================================
    // 2. CHECKOUT - THANH TOÁN
    // ========================================

    /**
     * Hiển thị trang thanh toán
     */
    @GetMapping("/checkout")
    public String showCheckoutPage(Model model, Principal principal) {
        try {
            if (principal == null) {
                return "redirect:/login";
            }

            // Lấy thông tin giỏ hàng
            List<CartItem> cartItems = cartService.getCurrentUserCartItems();

            if (cartItems.isEmpty()) {
                return "redirect:/user/cart";
            }

            // Tính toán
            BigDecimal subtotal = cartService.calculateSubtotal();
            BigDecimal shippingFee = subtotal.compareTo(new BigDecimal("500000")) >= 0
                    ? BigDecimal.ZERO
                    : new BigDecimal("30000");
            BigDecimal total = subtotal.add(shippingFee);

            // Lấy thông tin user
            String email = principal.getName();
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng"));

            model.addAttribute("cartItems", cartItems);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("shippingFee", shippingFee);
            model.addAttribute("total", total);
            model.addAttribute("user", user);

            return "user/checkout";

        } catch (Exception e) {
            model.addAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/user/cart";
        }
    }

    /**
     * Xử lý thanh toán
     */
    @PostMapping("/checkout/process")
    public String processCheckout(
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String address,
            @RequestParam(required = false) String note,
            @RequestParam String paymentMethod,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            if (principal == null) {
                return "redirect:/login";
            }

            // Tạo đơn hàng
            Order order = orderService.createOrder(fullName, phone, address, note, paymentMethod);

            redirectAttributes.addFlashAttribute("success",
                    "Đặt hàng thành công! Mã đơn hàng: #" + order.getOrderId());

            return "redirect:/user/orders/" + order.getOrderId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/user/checkout";
        }
    }

    // ========================================
    // 3. ORDER MANAGEMENT - QUẢN LÝ ĐƠN HÀNG
    // ========================================

    /**
     * Danh sách đơn hàng của user
     */
    @GetMapping("/orders")
    public String myOrders(Model model, Principal principal) {
        try {
            if (principal == null) {
                return "redirect:/login";
            }

            String email = principal.getName();
            List<Order> orders = orderService.findByUserEmail(email);

            model.addAttribute("orders", orders);

        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tải danh sách đơn hàng: " + e.getMessage());
        }

        return "user/orders";
    }

    /**
     * Chi tiết đơn hàng
     */
    @GetMapping("/orders/{id}")
    public String orderDetail(
            @PathVariable Integer id,
            Model model,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            if (principal == null) {
                return "redirect:/login";
            }

            Order order = orderService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

            // Kiểm tra quyền truy cập
            String email = principal.getName();
            if (!order.getUser().getEmail().equals(email)) {
                throw new RuntimeException("Bạn không có quyền xem đơn hàng này");
            }

            model.addAttribute("order", order);
            return "user/order-detail";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/user/orders";
        }
    }

    /**
     * Hủy đơn hàng
     */
    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(
            @PathVariable Integer id,
            @RequestParam(required = false) String reason,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            if (principal == null) {
                return "redirect:/login";
            }

            Order order = orderService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

            // Kiểm tra quyền
            String email = principal.getName();
            if (!order.getUser().getEmail().equals(email)) {
                throw new RuntimeException("Bạn không có quyền hủy đơn hàng này");
            }

            // Chỉ cho phép hủy đơn PENDING hoặc CONFIRMED
            if (!"PENDING".equals(order.getOrderStatus()) &&
                    !"CONFIRMED".equals(order.getOrderStatus())) {
                throw new RuntimeException("Không thể hủy đơn hàng ở trạng thái hiện tại");
            }

            orderService.cancelOrder(id, reason);

            redirectAttributes.addFlashAttribute("success", "Hủy đơn hàng thành công!");
            return "redirect:/user/orders/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/user/orders/" + id;
        }
    }

    // ========================================
    // 4. PROFILE - QUẢN LÝ THÔNG TIN CÁ NHÂN
    // ========================================

    /**
     * Trang thông tin cá nhân
     */
    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        try {
            if (principal == null) {
                return "redirect:/login";
            }

            String email = principal.getName();
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng"));

            model.addAttribute("user", user);

        } catch (Exception e) {
            model.addAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "user/profile";
    }

    /**
     * Cập nhật thông tin cá nhân
     */
    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam(required = false) String address,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            if (principal == null) {
                return "redirect:/login";
            }

            String email = principal.getName();
            userService.updateProfile(email, fullName, phone, address);

            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
            return "redirect:/user/profile";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/user/profile";
        }
    }

    /**
     * Đổi mật khẩu
     */
    @PostMapping("/profile/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            if (principal == null) {
                return "redirect:/login";
            }

            // Validate
            if (!newPassword.equals(confirmPassword)) {
                throw new RuntimeException("Mật khẩu xác nhận không khớp!");
            }

            if (newPassword.length() < 6) {
                throw new RuntimeException("Mật khẩu mới phải có ít nhất 6 ký tự!");
            }

            String email = principal.getName();
            userService.changePassword(email, currentPassword, newPassword);

            redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
            return "redirect:/user/profile";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/user/profile";
        }
    }
}
