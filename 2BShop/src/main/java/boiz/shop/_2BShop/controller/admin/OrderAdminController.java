package boiz.shop._2BShop.controller.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
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

    @Autowired
    private boiz.shop._2BShop.service.MailService mailService;

    @GetMapping
    public String listOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        try {
            Pageable pageable = PageRequest.of(page, 20, Sort.by("orderDate").descending());

            Page<Order> orders;

            // Apply filters in priority order
            if (keyword != null && !keyword.trim().isEmpty()) {
                orders = orderRepo.searchOrders(keyword, pageable);
            } else if (status != null && !status.trim().isEmpty() && !status.equals("ALL")) {
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

            // Calculate statistics
            long totalOrders = orderRepo.count();
            long pendingCount = orderRepo.countByOrderStatus("PENDING");
            long shippingCount = orderRepo.countByOrderStatus("SHIPPING");
            long deliveredCount = orderRepo.countByOrderStatus("DELIVERED");
            long cancelledCount = orderRepo.countByOrderStatus("CANCELLED");

            model.addAttribute("orders", orders);
            model.addAttribute("paymentMethods", paymentMethodRepo.findAll());
            model.addAttribute("selectedStatus", status);
            model.addAttribute("keyword", keyword);
            model.addAttribute("fromDate", fromDate);
            model.addAttribute("toDate", toDate);
            model.addAttribute("totalOrders", totalOrders);
            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("shippingCount", shippingCount);
            model.addAttribute("deliveredCount", deliveredCount);
            model.addAttribute("cancelledCount", cancelledCount);

            return "admin/orders";
        } catch (Exception e) {
            System.err.println("Error loading orders: " + e.getMessage());
            e.printStackTrace();
            
            // Ensure ALL model attributes are set to safe defaults
            model.addAttribute("orders", Page.empty());
            model.addAttribute("paymentMethods", new ArrayList<>());
            model.addAttribute("selectedStatus", "");
            model.addAttribute("keyword", "");
            model.addAttribute("fromDate", null);
            model.addAttribute("toDate", null);
            model.addAttribute("totalOrders", 0L);
            model.addAttribute("pendingCount", 0L);
            model.addAttribute("shippingCount", 0L);
            model.addAttribute("deliveredCount", 0L);
            model.addAttribute("cancelledCount", 0L);
            model.addAttribute("error", "Lỗi tải dữ liệu: " + e.getMessage());
            
            return "admin/orders";
        }
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Order order = orderRepo.findById(id).orElse(null);
            
            if (order == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng #" + id);
                return "redirect:/admin/orders";
            }

            List<OrderDetail> orderDetails = orderDetailRepo.findByOrderOrderId(id);
            List<String> validStatuses = getValidNextStatuses(order.getOrderStatus());

            model.addAttribute("order", order);
            model.addAttribute("orderDetails", orderDetails != null ? orderDetails : new ArrayList<>());
            model.addAttribute("validStatuses", validStatuses != null ? validStatuses : new ArrayList<>());
            
            return "admin/order-detail";
            
        } catch (Exception e) {
            System.err.println("Error loading order: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tải đơn hàng: " + e.getMessage());
            return "redirect:/admin/orders";
        }
    }

    @PostMapping("/update-status")
    public String updateOrderStatus(
            @RequestParam Integer orderId,
            @RequestParam String newStatus,
            RedirectAttributes redirectAttributes) {
        try {
            Order order = orderRepo.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Khoong tìm thấy đơn hàng với ID: " + orderId));

            List<String> validStatuses = getValidNextStatuses(order.getOrderStatus());
            if (!validStatuses.contains(newStatus)) {
                throw new RuntimeException("Không thể chuyển từ " + order.getOrderStatus() + " sang " + newStatus);
            }

            order.setOrderStatus(newStatus);
            order.setUpdatedDate(LocalDateTime.now());
            orderRepo.save(order);

            // Send email notification based on status change
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
                }
            } catch (Exception emailError) {
                // Log error but don't fail status update
                System.err.println("Failed to send status change email: " + emailError.getMessage());
            }

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
            RedirectAttributes redirectAttributes) {
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
                return Arrays.asList("CONFIRMED", "CANCELLED");
            case "CONFIRMED":
                return Arrays.asList("SHIPPING", "CANCELLED");
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
