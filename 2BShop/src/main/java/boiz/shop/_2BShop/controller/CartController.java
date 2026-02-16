package boiz.shop._2BShop.controller;

import boiz.shop._2BShop.entity.CartItem;
import boiz.shop._2BShop.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public String viewCart(Model model) {
        List<CartItem> cartItems = cartService.getCurrentUserCartItems();
        model.addAttribute("cartItems", cartItems);

        java.math.BigDecimal subtotal = cartService.calculateSubtotal();
        model.addAttribute("subtotal", subtotal);

        // Calculate total (subtotal + shipping) to match checkout logic
        // Shipping rule: < 500k -> 30k, else 0
        java.math.BigDecimal shippingFee = subtotal.compareTo(new java.math.BigDecimal("500000")) >= 0
                ? java.math.BigDecimal.ZERO
                : new java.math.BigDecimal("30000");
        java.math.BigDecimal total = subtotal.add(shippingFee);
        model.addAttribute("total", total);

        // Dummy summary for now to prevent NPE in view, or we can implement real
        // summary logic later
        // Just empty object or null check in view is enough, but view expects
        // summary.discountAmount
        // Let's passed a simple map or DTO if needed, but for now passing null is fine
        // because we check summary != null
        // However, user might expect the discount banner logic.
        // Let's add a placeholder summary if needed, but for now just ensure total is
        // there.
        model.addAttribute("summary", null); // Explicitly null to satisfy th:if check safely

        return "public/cart";
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Integer> request) {
        try {
            Integer watchId = request.get("watchId");
            Integer quantity = request.get("quantity");
            if (watchId == null || quantity == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Thiếu thông tin sản phẩm"));
            }
            cartService.addToCart(watchId, quantity);
            // Return updated cart count
            int count = cartService.getCartItemCount();
            return ResponseEntity.ok()
                    .body(Map.of("success", true, "message", "Đã thêm vào giỏ hàng", "cartItemCount", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<?> updateQuantity(@RequestBody Map<String, Integer> request) {
        try {
            Integer cartItemId = request.get("cartItemId");
            Integer quantity = request.get("quantity");
            if (cartItemId == null || quantity == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Thiếu thông tin cập nhật"));
            }
            cartService.updateQuantity(cartItemId, quantity);
            return ResponseEntity.ok().body(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/remove")
    @ResponseBody
    public ResponseEntity<?> removeCartItem(@RequestBody Map<String, Integer> request) {
        try {
            Integer cartItemId = request.get("cartItemId");
            if (cartItemId == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thiếu ID sản phẩm"));
            }
            cartService.removeCartItem(cartItemId);
            return ResponseEntity.ok().body(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCartCount() {
        Map<String, Object> response = new HashMap<>();
        try {
            int count = cartService.getCartItemCount();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("count", 0);
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/select")
    @ResponseBody
    public ResponseEntity<?> selectCartItem(@RequestBody Map<String, Object> payload) {
        try {
            Integer cartItemId = Integer.parseInt(payload.get("cartItemId").toString());
            Boolean isSelected = Boolean.parseBoolean(payload.get("isSelected").toString());
            cartService.updateSelection(cartItemId, isSelected);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã cập nhật chọn sản phẩm"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/select-all")
    @ResponseBody
    public ResponseEntity<?> selectAllItems(@RequestBody Map<String, Object> payload) {
        try {
            Boolean isSelected = Boolean.parseBoolean(payload.get("isSelected").toString());
            cartService.selectAll(isSelected);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã cập nhật chọn tất cả"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
