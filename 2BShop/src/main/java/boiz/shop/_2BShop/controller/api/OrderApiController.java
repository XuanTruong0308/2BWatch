package boiz.shop._2BShop.controller.api;

import boiz.shop._2BShop.entity.Order;
import boiz.shop._2BShop.entity.User;
import boiz.shop._2BShop.respository.OrderDetailRepository;
import boiz.shop._2BShop.respository.UserRepository;
import boiz.shop._2BShop.service.OrderService;
import boiz.shop._2BShop.service.OrderTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Order API")
@RestController
@RequestMapping("/api/v1/orders")
public class OrderApiController {

    @Autowired
    private OrderTrackingService orderTrackingService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Operation(summary = "Get my orders")
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> myOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = ApiAuthSupport.extractEmail(principal);
            if (email == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập");
                return ResponseEntity.status(401).body(response);
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            Pageable pageable = PageRequest.of(page, size);
            Page<Order> orders;
            if (status != null && !status.isEmpty()) {
                orders = orderTrackingService.getUserOrdersByStatus(user.getUserId(), status, pageable);
            } else {
                orders = orderTrackingService.getUserOrders(user.getUserId(), pageable);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("items", ApiDataMapper.mapOrders(orders.getContent()));
            data.put("page", ApiDataMapper.pageInfo(orders));
            data.put("selectedStatus", status);

            response.put("success", true);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Get order detail")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> detail(@PathVariable Integer id, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = ApiAuthSupport.extractEmail(principal);
            if (email == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập");
                return ResponseEntity.status(401).body(response);
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            Order order = orderTrackingService.getOrderDetails(id, user.getUserId());
            response.put("success", true);
            response.put("data", ApiDataMapper.orderDetail(order, orderDetailRepository.findByOrderOrderId(id)));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Cancel order")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancel(
            @PathVariable Integer id,
            @RequestBody(required = false) Map<String, Object> payload,
            Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = ApiAuthSupport.extractEmail(principal);
            if (email == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập");
                return ResponseEntity.status(401).body(response);
            }

            Order order = orderService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

            if (!order.getUser().getEmail().equals(email)) {
                throw new RuntimeException("Bạn không có quyền hủy đơn hàng này");
            }

            if (!"PENDING".equals(order.getOrderStatus()) && !"CONFIRMED".equals(order.getOrderStatus())) {
                throw new RuntimeException("Không thể hủy đơn hàng ở trạng thái hiện tại");
            }

            String reason = payload == null ? null : ApiValueParser.asString(payload.get("reason"));
            orderService.cancelOrder(id, reason);

            response.put("success", true);
            response.put("message", "Hủy đơn hàng thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
