package boiz.shop._2BShop.controller.api.admin;

import boiz.shop._2BShop.controller.api.ApiDataMapper;
import boiz.shop._2BShop.controller.api.ApiValueParser;
import boiz.shop._2BShop.entity.Order;
import boiz.shop._2BShop.respository.OrderDetailRepository;
import boiz.shop._2BShop.respository.OrderRepository;
import boiz.shop._2BShop.respository.PaymentMethodRepository;
import boiz.shop._2BShop.service.MailService;
import boiz.shop._2BShop.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Admin Order API")
@RestController
@RequestMapping("/api/v1/admin/orders")
public class OrderAdminApiController {

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private OrderDetailRepository orderDetailRepo;

    @Autowired
    private PaymentMethodRepository paymentMethodRepo;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MailService mailService;

    @Operation(summary = "Get orders for admin")
    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate) {
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
            Page<Order> orders;

            if (keyword != null && !keyword.trim().isEmpty()) {
                orders = orderRepo.searchOrders(keyword, pageable);
            } else if (status != null && !status.trim().isEmpty() && !"ALL".equals(status)) {
                if (fromDate != null && toDate != null) {
                    orders = orderRepo.findByOrderStatusAndOrderDateBetween(
                            status,
                            fromDate.atStartOfDay(),
                            toDate.atTime(LocalTime.MAX),
                            pageable);
                } else {
                    orders = orderRepo.findByOrderStatus(status, pageable);
                }
            } else if (fromDate != null && toDate != null) {
                orders = orderRepo.findByOrderDateBetween(
                        fromDate.atStartOfDay(),
                        toDate.atTime(LocalTime.MAX),
                        pageable);
            } else {
                orders = orderRepo.findAll(pageable);
            }

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalOrders", orderRepo.count());
            stats.put("pendingCount", orderRepo.countByOrderStatus("PENDING"));
            stats.put("shippingCount", orderRepo.countByOrderStatus("SHIPPING"));
            stats.put("deliveredCount", orderRepo.countByOrderStatus("DELIVERED"));
            stats.put("cancelledCount", orderRepo.countByOrderStatus("CANCELLED"));

            Map<String, Object> data = new HashMap<>();
            data.put("items", ApiDataMapper.mapOrders(orders.getContent()));
            data.put("page", ApiDataMapper.pageInfo(orders));
            data.put("paymentMethods", paymentMethodRepo.findAll().stream().map(ApiDataMapper::paymentMethod).toList());
            data.put("stats", stats);
                Map<String, Object> filters = new HashMap<>();
                filters.put("status", status);
                filters.put("keyword", keyword);
                filters.put("fromDate", fromDate);
                filters.put("toDate", toDate);
                data.put("filters", filters);

            response.put("success", true);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi tải dữ liệu: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Get admin order detail")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> detail(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Order order = orderRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng #" + id));

            Map<String, Object> data = new HashMap<>();
            data.put("order", ApiDataMapper.orderDetail(order, orderDetailRepo.findByOrderOrderId(id)));
            data.put("validStatuses", getValidNextStatuses(order.getOrderStatus()));

            response.put("success", true);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Update order status")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            String newStatus = ApiValueParser.asString(payload.get("newStatus"));
            if (newStatus == null || newStatus.isBlank()) {
                throw new RuntimeException("Thiếu trạng thái mới");
            }

            Order order = orderRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + id));

            List<String> validStatuses = getValidNextStatuses(order.getOrderStatus());
            if (!validStatuses.contains(newStatus)) {
                throw new RuntimeException("Không thể chuyển từ " + order.getOrderStatus() + " sang " + newStatus);
            }

            order.setOrderStatus(newStatus);
            order.setUpdatedDate(LocalDateTime.now());
            orderRepo.save(order);

            try {
                String customerEmail = order.getUser().getEmail();
                String customerName = order.getReceiverName();
                String orderCode = "ORD" + String.format("%06d", order.getOrderId());

                switch (newStatus) {
                    case "SHIPPING":
                        mailService.sendShippingEmail(customerEmail, customerName, orderCode);
                        break;
                    case "DELIVERED":
                        mailService.sendDeliveredEmail(customerEmail, customerName, orderCode);
                        break;
                    case "COMPLETED":
                        mailService.sendCompletedEmail(customerEmail, customerName, orderCode);
                        break;
                    case "CANCELLED":
                        mailService.sendCancelledEmail(customerEmail, customerName, orderCode);
                        break;
                    default:
                        break;
                }
            } catch (Exception ignored) {
            }

            response.put("success", true);
            response.put("message", "Cập nhật trạng thái thành công!");
            response.put("order", ApiDataMapper.orderSummary(order));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Cancel order")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancel(@PathVariable Integer id, @RequestBody(required = false) Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            String reason = payload == null ? null : ApiValueParser.asString(payload.get("reason"));
            orderService.cancelOrder(id, reason);
            response.put("success", true);
            response.put("message", "Đã hủy đơn hàng thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private List<String> getValidNextStatuses(String currentStatus) {
        switch (currentStatus) {
            case "PENDING":
                return Arrays.asList("CONFIRMED", "CANCELLED");
            case "CONFIRMED":
                return Arrays.asList("SHIPPING", "CANCELLED");
            case "SHIPPING":
                return Collections.singletonList("DELIVERED");
            case "DELIVERED":
            case "CANCELLED":
            default:
                return Collections.emptyList();
        }
    }
}
