package boiz.shop._2BShop.api.controller.user;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import boiz.shop._2BShop.api.mapper.ApiMapper;
import boiz.shop._2BShop.api.request.CancelOrderRequest;
import boiz.shop._2BShop.api.response.ApiResponse;
import boiz.shop._2BShop.api.response.PaginatedResponse;
import boiz.shop._2BShop.api.util.CurrentUserService;
import boiz.shop._2BShop.entity.Order;
import boiz.shop._2BShop.entity.User;
import boiz.shop._2BShop.respository.OrderRepository;
import boiz.shop._2BShop.service.OrderService;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderApiController {

    private final CurrentUserService currentUserService;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final ApiMapper apiMapper;

    public OrderApiController(
            CurrentUserService currentUserService,
            OrderRepository orderRepository,
            OrderService orderService,
            ApiMapper apiMapper) {
        this.currentUserService = currentUserService;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.apiMapper = apiMapper;
    }

    @GetMapping
    public PaginatedResponse<?> listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        User user = currentUserService.getCurrentUserOrThrow();
        Pageable pageable = PageRequest.of(page, size);
        if (status != null && !status.isBlank()) {
            return PaginatedResponse.from(
                    orderRepository.findByUserUserIdAndOrderStatusOrderByOrderDateDesc(user.getUserId(), status, pageable),
                    apiMapper::toOrderDto);
        }
        return PaginatedResponse.from(
                orderRepository.findByUserUserIdOrderByOrderDateDesc(user.getUserId(), pageable),
                apiMapper::toOrderDto);
    }

    @GetMapping("/{id}")
    public ApiResponse<?> orderDetail(@PathVariable Integer id) {
        User user = currentUserService.getCurrentUserOrThrow();
        Order order = orderService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        if (!order.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Bạn không có quyền xem đơn hàng này");
        }
        return ApiResponse.success(apiMapper.toOrderDto(order));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<?> cancelOrder(@PathVariable Integer id, @RequestBody(required = false) CancelOrderRequest request) {
        User user = currentUserService.getCurrentUserOrThrow();
        Order order = orderService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        if (!order.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Bạn không có quyền hủy đơn hàng này");
        }
        orderService.cancelOrder(id, request != null ? request.reason() : null);
        return ApiResponse.success("Hủy đơn hàng thành công", apiMapper.toOrderDto(
                orderService.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"))));
    }
}
