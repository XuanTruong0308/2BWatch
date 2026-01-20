package boiz.shop._2BShop.controller.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import boiz.shop._2BShop.entity.Order;
import boiz.shop._2BShop.entity.OrderDetail;
import boiz.shop._2BShop.respository.OrderDetailRepository;
import boiz.shop._2BShop.respository.OrderRepository;
import boiz.shop._2BShop.respository.PaymentMethodRepository;
import boiz.shop._2BShop.service.OrderService;

@Controller
@RequestMapping("/admin/orders")
public class OrderAdminController {
    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private OrderDetailRepository orderDetailRepo;

    @Autowired
    private PaymentMethodRepository paymentMethodRepo;

    @Autowired
    private OrderService orderService;

    @GetMapping
    public String listOrders(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
        @RequestParam(defaultValue = "0") int page,
        Model model
    ) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by("orderDate").descending());

        Page<Order> orders;

        if(keyword != null && !keyword.trim().isEmpty()) {
            orders = orderRepo.searchOrders(keyword, pageable);
        }else if(status != null && !status.trim().isEmpty()) {
            orders = orderRepo.findByOrderStatusAndOrderDateBetween(
                status, 
                fromDate.atStartOfDay(), 
                toDate.atTime(LocalTime.MAX),
                pageable
            );
        } else if(status != null && !status.equals("ALL")) {
            orders = orderRepo.findByOrderStatus(status, pageable);
        } else if(fromDate != null && toDate != null) {
            orders = orderRepo.findByOrderDateBetween(
                fromDate.atStartOfDay(),
                toDate.atTime(LocalTime.MAX),
                pageable
            );
        } else {
            orders = orderRepo.findAll(pageable);
        }

        model.addAttribute("orders", orders);
        model.addAttribute("paymentMethods", paymentMethodRepo.findAll());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);

        return "admin/orders";
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Integer id, Model model) {
        Order order = orderRepo.findById(id)
            .orElseThrow(() -> new RuntimeException
            ("Không tìm thấy id đơn hàng: "+ id));

        List<OrderDetail> orderDetails = orderDetailRepo.findByOrderOrderId(id);

        List<String> validStatuses = getValidNextStatuses(order.getOrderStatus());

        model.addAttribute("order", order);
        model.addAttribute("orderDetails", orderDetails);
        model.addAttribute("validStatuses", validStatuses);
        return "admin/order-detail";
    }

    @PostMapping("/update-status")
    public String updateOrderStatus(
        @RequestParam Integer orderId,
        @RequestParam String newStatus,
        RedirectAttributes redirectAttributes
    ) {
        try {
            Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Khoong tìm thấy đơn hàng với ID: " + orderId));

            List<String> validStatuses = getValidNextStatuses(order.getOrderStatus());
            if(!validStatuses.contains(newStatus)) {
                throw new RuntimeException("Không thể chuyển từ " + order.getOrderStatus() + " sang " + newStatus);
            }

            order.setOrderStatus(newStatus);
            order.setUpdatedDate(LocalDateTime.now());
            orderRepo.save(order);

            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + orderId;
    }

    @PostMapping("/cancel/{id}")
    public String cancelOrder(
        @PathVariable Integer id,
        @RequestParam(required = false) String reason,
        RedirectAttributes redirectAttributes
    ) {
        try {
            orderService.cancelOrder(id, reason);
            
            redirectAttributes.addFlashAttribute("success", "Đã hủy đơn hàng thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "redirect:/admin/orders/" + id;
    }

    private List<String> getValidNextStatuses(String currentStatus) {
        switch (currentStatus) {
            case "PENDING":
                return Arrays.asList("CONFIRMED", "CANCELED");
            case "CONFIRMED":
                return Arrays.asList("SHIPPING", "CANCELED");
            case "SHIPPING":
                return Arrays.asList("DELIVERED");
            case "DELIVERED":
            case "CANCELLED":
                return Collections.emptyList();
            default:
                return Collections.emptyList();
        }
    }
}
