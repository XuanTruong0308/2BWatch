package boiz.shop._2BShop.controller.api;

import boiz.shop._2BShop.entity.Order;
import boiz.shop._2BShop.entity.User;
import boiz.shop._2BShop.respository.OrderDetailRepository;
import boiz.shop._2BShop.respository.UserRepository;
import boiz.shop._2BShop.service.BankAccountService;
import boiz.shop._2BShop.service.CartService;
import boiz.shop._2BShop.service.CheckoutService;
import boiz.shop._2BShop.service.CheckoutService.CheckoutSummary;
import boiz.shop._2BShop.service.PhoneVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Checkout API")
@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutApiController {

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private CartService cartService;

    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PhoneVerificationService phoneVerificationService;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Operation(summary = "Get checkout summary")
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> summary(
            @RequestParam(required = false) String couponCode) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = ApiAuthSupport.currentAuthenticatedEmail();
            if (email == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập để thanh toán");
                return ResponseEntity.status(401).body(response);
            }

            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null
                    && "GOOGLE".equalsIgnoreCase(user.getProvider())
                    && phoneVerificationService.needsPhoneVerification(user)) {
                response.put("success", false);
                response.put("message", "Vui lòng cập nhật số điện thoại để tiếp tục đặt hàng");
                response.put("requirePhone", true);
                return ResponseEntity.badRequest().body(response);
            }

            var cartItems = cartService.getSelectedCartItems();
            if (cartItems.isEmpty()) {
                response.put("success", false);
                response.put("message", "Giỏ hàng trống!");
                return ResponseEntity.badRequest().body(response);
            }

            CheckoutSummary summary = checkoutService.calculateTotals(couponCode);

            Map<String, Object> data = new HashMap<>();
            data.put("cartItems", ApiDataMapper.mapCartItems(cartItems));
            data.put("summary", ApiDataMapper.checkoutSummary(summary));
            data.put("bankAccounts", ApiDataMapper.mapBankAccounts(bankAccountService.getActiveBankAccounts()));
            data.put("user", ApiDataMapper.userSummary(user));

            response.put("success", true);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Place order")
    @PostMapping("/place-order")
    public ResponseEntity<Map<String, Object>> placeOrder(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = ApiAuthSupport.currentAuthenticatedEmail();
            if (email == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập để thanh toán");
                return ResponseEntity.status(401).body(response);
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng"));

            String receiverName = ApiValueParser.asString(payload.get("receiverName"));
            String phone = ApiValueParser.asString(payload.get("phone"));
            String address = ApiValueParser.asString(payload.get("address"));
            String paymentMethod = ApiValueParser.asString(payload.get("paymentMethod"));
            String notes = ApiValueParser.asString(payload.get("notes"));
            String couponCode = ApiValueParser.asString(payload.get("couponCode"));
            Integer bankAccountId = ApiValueParser.asInteger(payload.get("bankAccountId"));

            Order order = checkoutService.placeOrder(
                    user,
                    receiverName,
                    phone,
                    address,
                    notes,
                    paymentMethod,
                    couponCode,
                    bankAccountId);

            Map<String, Object> data = new HashMap<>();
            data.put("orderId", order.getOrderId());
            data.put("orderCode", "ORD" + String.format("%06d", order.getOrderId()));
            data.put("order", ApiDataMapper.orderSummary(order));

            response.put("success", true);
            response.put("message", "Đặt hàng thành công!");
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi đặt hàng: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Get order confirmation")
    @GetMapping("/confirmation/{orderId}")
    public ResponseEntity<Map<String, Object>> orderConfirmation(@PathVariable Integer orderId) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = ApiAuthSupport.currentAuthenticatedEmail();
            if (email == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập");
                return ResponseEntity.status(401).body(response);
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng!"));

            Order order = checkoutService.getOrderById(orderId);
            if (!order.getUser().getUserId().equals(user.getUserId())) {
                response.put("success", false);
                response.put("message", "Bạn không có quyền xem đơn hàng này!");
                return ResponseEntity.status(403).body(response);
            }

            response.put("success", true);
            response.put("data", ApiDataMapper.orderDetail(order, orderDetailRepository.findByOrderOrderId(orderId)));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Không thể tải trang xác nhận: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
