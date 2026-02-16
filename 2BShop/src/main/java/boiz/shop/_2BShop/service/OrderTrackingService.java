package boiz.shop._2BShop.service;

import boiz.shop._2BShop.entity.Order;

import boiz.shop._2BShop.respository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OrderTrackingService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MailService mailService;

    /**
     * Get user's orders with pagination
     */
    public Page<Order> getUserOrders(Integer userId, Pageable pageable) {
        return orderRepository.findByUserUserIdOrderByOrderDateDesc(userId, pageable);
    }

    /**
     * Get user's orders by status
     */
    public Page<Order> getUserOrdersByStatus(Integer userId, String status, Pageable pageable) {
        return orderRepository.findByUserUserIdAndOrderStatusOrderByOrderDateDesc(userId, status, pageable);
    }

    /**
     * Get order details with security check
     */
    public Order getOrderDetails(Integer orderId, Integer userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // Security check: ensure user owns this order
        if (!order.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xem đơn hàng này");
        }

        return order;
    }

    /**
     * Update order status and send notification email
     */
    @Transactional
    public void updateOrderStatus(Integer orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // String oldStatus = order.getOrderStatus(); // Unused
        order.setOrderStatus(newStatus);
        order.setUpdatedDate(LocalDateTime.now());
        orderRepository.save(order);

        // Send email notification based on new status
        sendStatusNotificationEmail(order, newStatus);
    }

    /**
     * Send email notification based on order status
     */
    /**
     * Send email notification based on order status
     */
    private void sendStatusNotificationEmail(Order order, String status) {
        String customerEmail = order.getUser().getEmail();
        String customerName = order.getReceiverName();
        String orderCode = "ORD" + String.format("%06d", order.getOrderId());

        switch (status) {
            case "CONFIRMED":
                mailService.sendOrderConfirmation(order, order.getOrderDetails());
                break;
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
    }
}
