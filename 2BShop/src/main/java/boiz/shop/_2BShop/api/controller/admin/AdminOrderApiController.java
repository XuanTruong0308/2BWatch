package boiz.shop._2BShop.api.controller.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import boiz.shop._2BShop.api.mapper.ApiMapper;
import boiz.shop._2BShop.api.request.CancelOrderRequest;
import boiz.shop._2BShop.api.request.OrderStatusUpdateRequest;
import boiz.shop._2BShop.api.response.ApiResponse;
import boiz.shop._2BShop.api.response.PaginatedResponse;
import boiz.shop._2BShop.entity.Order;
import boiz.shop._2BShop.respository.OrderRepository;
import boiz.shop._2BShop.service.MailService;
import boiz.shop._2BShop.service.OrderService;

@RestController
@RequestMapping("/api/v1/admin/orders")
public class AdminOrderApiController {

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final MailService mailService;
    private final ApiMapper apiMapper;

    public AdminOrderApiController(
            OrderRepository orderRepository,
            OrderService orderService,
            MailService mailService,
            ApiMapper apiMapper) {
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.mailService = mailService;
        this.apiMapper = apiMapper;
    }

    @GetMapping
    public ApiResponse<Map<String, ?>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
            @RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("orderDate").descending());
        Page<Order> orders;
        if (keyword != null && !keyword.isBlank()) {
            orders = orderRepository.searchOrders(keyword, pageable);
        } else if (status != null && !status.isBlank() && !"ALL".equals(status)) {
            if (fromDate != null && toDate != null) {
                orders = orderRepository.findByOrderStatusAndOrderDateBetween(
                        status,
                        fromDate.atStartOfDay(),
                        toDate.atTime(LocalTime.MAX),
                        pageable);
            } else {
                orders = orderRepository.findByOrderStatus(status, pageable);
            }
        } else if (fromDate != null && toDate != null) {
            orders = orderRepository.findByOrderDateBetween(fromDate.atStartOfDay(), toDate.atTime(LocalTime.MAX), pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }
        return ApiResponse.success(Map.of(
                "orders", PaginatedResponse.from(orders, apiMapper::toOrderDto),
                "stats", Map.of(
                        "totalOrders", orderRepository.count(),
                        "pendingCount", orderRepository.countByOrderStatus("PENDING"),
                        "shippingCount", orderRepository.countByOrderStatus("SHIPPING"),
                        "deliveredCount", orderRepository.countByOrderStatus("DELIVERED"),
                        "cancelledCount", orderRepository.countByOrderStatus("CANCELLED"))));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> detail(@PathVariable Integer id) {
        Order order = findOrder(id);
        return ApiResponse.success(Map.of(
                "order", apiMapper.toOrderDto(order),
                "validStatuses", getValidNextStatuses(order.getOrderStatus())));
    }

    @PostMapping("/{id}/status")
    public ApiResponse<?> updateStatus(@PathVariable Integer id, @RequestBody OrderStatusUpdateRequest request) {
        Order order = findOrder(id);
        List<String> validStatuses = getValidNextStatuses(order.getOrderStatus());
        if (!validStatuses.contains(request.newStatus())) {
            throw new RuntimeException("Không thể chuyển từ " + order.getOrderStatus() + " sang " + request.newStatus());
        }
        order.setOrderStatus(request.newStatus());
        order.setUpdatedDate(LocalDateTime.now());
        orderRepository.save(order);
        try {
            String email = order.getUser().getEmail();
            String name = order.getReceiverName();
            String orderCode = "ORD" + String.format("%06d", order.getOrderId());
            switch (request.newStatus()) {
                case "SHIPPING" -> mailService.sendShippingEmail(email, name, orderCode);
                case "DELIVERED" -> mailService.sendDeliveredEmail(email, name, orderCode);
                case "COMPLETED" -> mailService.sendCompletedEmail(email, name, orderCode);
                case "CANCELLED" -> mailService.sendCancelledEmail(email, name, orderCode);
                default -> {
                }
            }
        } catch (Exception ignored) {
        }
        return ApiResponse.success("Cập nhật trạng thái thành công", apiMapper.toOrderDto(findOrder(id)));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<?> cancel(@PathVariable Integer id, @RequestBody(required = false) CancelOrderRequest request) {
        orderService.cancelOrder(id, request != null ? request.reason() : null);
        return ApiResponse.success("Đã hủy đơn hàng thành công", apiMapper.toOrderDto(findOrder(id)));
    }

    private Order findOrder(Integer id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
    }

    private List<String> getValidNextStatuses(String currentStatus) {
        return switch (currentStatus) {
            case "PENDING" -> Arrays.asList("CONFIRMED", "CANCELLED");
            case "CONFIRMED" -> Arrays.asList("SHIPPING", "CANCELLED");
            case "SHIPPING" -> List.of("DELIVERED");
            default -> Collections.emptyList();
        };
    }
}
