package boiz.shop._2BShop.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import boiz.shop._2BShop.service.CartService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Cart API")
@RestController
@RequestMapping("/api/v1/cart")
public class CartApiController {

    @Autowired
    private CartService cartService;

    // GET /api/v1/cart
    @Operation(summary = "Get current cart items")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCart() {
        Map<String, Object> res = new HashMap<>();
        try {
            var items = cartService.getCurrentUserCartItems();
            var subtotal = cartService.calculateSubtotal();
            var shippingFee = subtotal.compareTo(new BigDecimal("500000")) >= 0
                    ? BigDecimal.ZERO
                    : new BigDecimal("30000");
            var total = subtotal.add(shippingFee);

            res.put("success", true);
            res.put("items", ApiDataMapper.mapCartItems(items));
            res.put("subtotal", subtotal);
            res.put("shippingFee", shippingFee);
            res.put("total", total);
            res.put("cartItemCount", items.size());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    // GET /api/v1/cart/count
    @Operation(summary = "Get cart item count")
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> count() {
        try {
            int count = cartService.getCartItemCount();
            return ResponseEntity.ok(Map.of("success", true, "count", count));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", true, "count", 0));
        }
    }

    // POST /api/v1/cart/add
    @Operation(summary = "Add item to cart")
    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody Map<String, Integer> request) {
        try {
            Integer watchId = request.get("watchId");
            Integer quantity = request.get("quantity");
            if (watchId == null || quantity == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Thiếu thông tin sản phẩm"));
            }
            cartService.addToCart(watchId, quantity);
            int count = cartService.getCartItemCount();
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã thêm vào giỏ hàng", "cartItemCount", count));
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("đăng nhập")) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", e.getMessage(), "needLogin", true));
            }
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // POST /api/v1/cart/update
    @Operation(summary = "Update cart item quantity")
    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestBody Map<String, Integer> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer cartItemId = request.get("cartItemId");
            Integer quantity = request.get("quantity");
            if (cartItemId == null || quantity == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Thiếu thông tin cập nhật"));
            }

            cartService.updateQuantity(cartItemId, quantity);

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
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // POST /api/v1/cart/remove
    @Operation(summary = "Remove cart item")
    @PostMapping("/remove")
    public ResponseEntity<?> remove(@RequestBody Map<String, Integer> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer cartItemId = request.get("cartItemId");
            if (cartItemId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Thiếu ID sản phẩm"));
            }

            cartService.removeCartItem(cartItemId);

            BigDecimal subtotal = cartService.calculateSubtotal();
            BigDecimal shippingFee = subtotal.compareTo(new BigDecimal("500000")) >= 0
                    ? BigDecimal.ZERO
                    : new BigDecimal("30000");
            BigDecimal total = subtotal.add(shippingFee);

            response.put("success", true);
            response.put("message", "Đã xóa khỏi giỏ hàng!");
            response.put("subtotal", subtotal);
            response.put("shippingFee", shippingFee);
            response.put("total", total);
            response.put("cartItemCount", cartService.getCartItemCount());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // POST /api/v1/cart/select
    @Operation(summary = "Select/deselect cart item")
    @PostMapping("/select")
    public ResponseEntity<?> select(@RequestBody Map<String, Object> payload) {
        try {
            Integer cartItemId = Integer.parseInt(payload.get("cartItemId").toString());
            Boolean isSelected = Boolean.parseBoolean(payload.get("isSelected").toString());
            cartService.updateSelection(cartItemId, isSelected);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã cập nhật chọn sản phẩm"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // POST /api/v1/cart/select-all
    @Operation(summary = "Select/deselect all cart items")
    @PostMapping("/select-all")
    public ResponseEntity<?> selectAll(@RequestBody Map<String, Object> payload) {
        try {
            Boolean isSelected = Boolean.parseBoolean(payload.get("isSelected").toString());
            cartService.selectAll(isSelected);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã cập nhật chọn tất cả"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @Operation(summary = "Clear all cart items")
    @PostMapping("/clear")
    public ResponseEntity<Map<String, Object>> clear() {
        Map<String, Object> response = new HashMap<>();
        try {
            cartService.clearCart();
            response.put("success", true);
            response.put("message", "Đã xóa toàn bộ giỏ hàng!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
