package boiz.shop._2BShop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import boiz.shop._2BShop.entity.*;
import boiz.shop._2BShop.respository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service xử lý đơn hàng
 */
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private WatchRepository watchRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private MailService mailService;

    /**
     * Tìm tất cả orders
     */
    public Page<Order> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    /**
     * Tìm orders theo status
     */
    public Page<Order> findByStatus(String status, Pageable pageable) {
        return orderRepository.findByOrderStatus(status, pageable);
    }

    /**
     * Tìm order theo ID
     */
    public Optional<Order> findById(Integer id) {
        return orderRepository.findById(id);
    }

    /**
     * Tìm orders của user theo email
     */
    public List<Order> findByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    /**
     * Tạo đơn hàng mới
     */
    @Transactional
    public Order createOrder(String fullName, String phone, String address,
            String note, String paymentMethod) {
        // Lấy user hiện tại
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // Lấy cart items
        List<CartItem> cartItems = cartService.getCurrentUserCartItems();

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống!");
        }

        // Tính toán
        BigDecimal subtotal = cartService.calculateSubtotal();
        BigDecimal shippingFee = subtotal.compareTo(new BigDecimal("500000")) >= 0
                ? BigDecimal.ZERO
                : new BigDecimal("30000");
        BigDecimal totalAmount = subtotal.add(shippingFee);

        // Lấy PaymentMethod từ database
        PaymentMethod paymentMethodEntity = paymentMethodRepository.findByMethodName(paymentMethod)
                .orElseThrow(() -> new RuntimeException("Phương thức thanh toán không hợp lệ"));

        // Tạo order
        Order order = new Order();
        order.setUser(user);
        order.setReceiverName(fullName);
        order.setShippingPhone(phone);
        order.setShippingAddress(address);
        order.setNotes(note);
        order.setPaymentMethod(paymentMethodEntity);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderStatus("PENDING");
        order.setTotalAmount(totalAmount);

        order = orderRepository.save(order);

        // Tạo order details và giảm stock
        for (CartItem item : cartItems) {
            Watch watch = item.getWatch();

            // Check stock
            if (watch.getStockQuantity() < item.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + watch.getWatchName() + " không đủ hàng!");
            }

            // Tạo order detail - LƯU GIÁ TẠI THỜI ĐIỂM ĐẶT HÀNG
            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setWatch(watch);
            detail.setQuantity(item.getQuantity());

            // unit_price: Giá gốc tại thời điểm đặt hàng
            detail.setUnitPrice(watch.getPrice());

            // discount_amount: Số tiền giảm giá PER UNIT
            BigDecimal discountPerUnit = BigDecimal.ZERO;
            if (watch.getDiscountPercent() != null && watch.getDiscountPercent() > 0) {
                discountPerUnit = watch.getPrice()
                        .multiply(BigDecimal.valueOf(watch.getDiscountPercent()))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
            detail.setDiscountAmount(discountPerUnit);

            // subtotal: (unit_price - discount_amount) * quantity
            BigDecimal priceAfterDiscount = watch.getPrice().subtract(discountPerUnit);
            detail.setSubtotal(priceAfterDiscount.multiply(new BigDecimal(detail.getQuantity())));

            orderDetailRepository.save(detail);

            // Giảm stock và tăng sold_count
            watch.setStockQuantity(watch.getStockQuantity() - item.getQuantity());
            watch.setSoldCount(watch.getSoldCount() + item.getQuantity());
            watch.setUpdatedDate(LocalDateTime.now());
            watchRepository.save(watch);
        }

        // Tạo và lưu payment transaction
        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setOrder(order);
        paymentTransaction.setPaymentMethod(paymentMethodEntity);
        paymentTransaction.setAmount(totalAmount);
        paymentTransaction.setStatus("PENDING");
        paymentTransaction.setTransactionDate(LocalDateTime.now());
        paymentTransactionRepository.save(paymentTransaction);

        // Clear cart
        cartService.clearCart();

        // Gửi email xác nhận đơn hàng với hóa đơn chi tiết
        try {
            List<OrderDetail> orderDetailsList = orderDetailRepository.findByOrder(order);
            mailService.sendOrderConfirmation(order, orderDetailsList);
        } catch (Exception e) {
            // Log lỗi nhưng không throw exception để không ảnh hưởng đến flow đặt hàng
            System.err.println("Lỗi gửi email xác nhận đơn hàng: " + e.getMessage());
        }

        return order;
    }

    /**
     * Cập nhật status của order
     */
    @Transactional
    public void updateStatus(Integer orderId, String status, String note) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        order.setOrderStatus(status);
        if (note != null && !note.isEmpty()) {
            String currentNotes = order.getNotes();
            order.setNotes((currentNotes != null ? currentNotes : "") + "\n" + note);
        }
        order.setUpdatedDate(LocalDateTime.now());

        orderRepository.save(order);

        // TODO: Gửi email thông báo cho khách hàng
    }

    /**
     * Hủy đơn hàng
     */
    @Transactional
    public void cancelOrder(Integer orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // Chỉ cho phép hủy đơn PENDING hoặc CONFIRMED
        if (!"PENDING".equals(order.getOrderStatus()) &&
                !"CONFIRMED".equals(order.getOrderStatus())) {
            throw new RuntimeException("Không thể hủy đơn hàng ở trạng thái: " + order.getOrderStatus());
        }

        // Hoàn lại stock và giảm sold_count (nếu đơn đã hoàn thành)
        List<OrderDetail> details = orderDetailRepository.findByOrder(order);
        for (OrderDetail detail : details) {
            Watch watch = detail.getWatch();
            watch.setStockQuantity(watch.getStockQuantity() + detail.getQuantity());

            // Nếu đơn đã hoàn thành thì giảm sold_count
            if ("COMPLETED".equals(order.getOrderStatus()) || "DELIVERED".equals(order.getOrderStatus())) {
                watch.setSoldCount(Math.max(0, watch.getSoldCount() - detail.getQuantity()));
            }

            watch.setUpdatedDate(LocalDateTime.now());
            watchRepository.save(watch);
        }

        // Cập nhật status
        order.setOrderStatus("CANCELLED");
        if (reason != null && !reason.isEmpty()) {
            String currentNotes = order.getNotes();
            order.setNotes((currentNotes != null ? currentNotes : "") + "\nLý do hủy: " + reason);
        }
        order.setUpdatedDate(LocalDateTime.now());

        orderRepository.save(order);
    }
}
