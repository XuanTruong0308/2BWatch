package boiz.shop._2BShop.service;

import boiz.shop._2BShop.entity.*;
import boiz.shop._2BShop.respository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class CheckoutService {

    @Autowired
    private CartService cartService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private WatchRepository watchRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    // @Autowired
    // private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private MailService mailService;

    /**
     * Get order by ID
     */
    public Order getOrderById(Integer orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));
    }

    /**
     * DTO for checkout summary calculation
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class CheckoutSummary {
        private BigDecimal subtotal;
        private BigDecimal discountAmount;
        private BigDecimal shippingFee;
        private BigDecimal totalAmount;
        private BigDecimal depositAmount;
        private boolean depositRequired;
        private String couponCode;
    }

    /**
     * Calculate checkout totals
     */
    public CheckoutSummary calculateTotals(String couponCode) {
        // 1. Calculate subtotal
        BigDecimal subtotal = cartService.calculateSubtotal();

        // 2. Calculate discount (coupon)
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (couponCode != null && !couponCode.isEmpty()) {
            discountAmount = couponService.calculateDiscount(couponCode, subtotal);
        }

        // 3. Calculate shipping fee (Free for orders >= 500k)
        BigDecimal shippingFee = subtotal.compareTo(new BigDecimal("500000")) >= 0
                ? BigDecimal.ZERO
                : new BigDecimal("30000");

        // 4. Calculate total
        BigDecimal totalAmount = subtotal.subtract(discountAmount).add(shippingFee);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        // 5. Calculate deposit
        BigDecimal ONE_BILLION = new BigDecimal("1000000000");
        boolean depositRequired = totalAmount.compareTo(ONE_BILLION) >= 0;
        BigDecimal depositAmount = BigDecimal.ZERO;

        if (depositRequired) {
            // 25% deposit for orders >= 1 billion
            depositAmount = totalAmount.multiply(new BigDecimal("0.25")).setScale(0, java.math.RoundingMode.HALF_UP);
        }

        return new CheckoutSummary(
                subtotal,
                discountAmount,
                shippingFee,
                totalAmount,
                depositAmount,
                depositRequired,
                couponCode);
    }

    /**
     * Place an order
     */
    @Transactional
    public Order placeOrder(User user, String receiverName, String phone, String address,
            String notes, String paymentMethod, String couponCode, Integer bankAccountId) {

        // 1. Get cart items
        List<CartItem> cartItems = cartService.getSelectedCartItems();
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống!");
        }

        // 2. Validate stock again
        for (CartItem item : cartItems) {
            if (item.getWatch().getStockQuantity() < item.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + item.getWatch().getWatchName() + " không đủ hàng!");
            }
        }

        // 3. Calculate totals
        CheckoutSummary summary = calculateTotals(couponCode);

        // 4. Create Order
        Order order = new Order();
        order.setUser(user);
        order.setReceiverName(receiverName);
        order.setShippingPhone(phone);
        order.setShippingAddress(address);
        order.setNotes(notes);
        // order.setPaymentMethod(paymentMethod); // Removed: passes String, entity set
        // below

        // Retrieve and set PaymentMethod entity (required by legacy constraint)
        if (paymentMethod != null) {
            PaymentMethod pmEntity = paymentMethodRepository.findByMethodName(paymentMethod)
                    .orElseThrow(() -> new RuntimeException("Phương thức thanh toán không hợp lệ: " + paymentMethod));
            order.setPaymentMethod(pmEntity);

            // Auto-create PaymentTransaction for the chosen method
            PaymentTransaction txn = new PaymentTransaction();
            txn.setOrder(order);
            txn.setPaymentMethod(pmEntity);
            txn.setAmount(summary.getTotalAmount());
            txn.setStatus("PENDING");
            txn.setTransactionDate(LocalDateTime.now());

            // Add to order for cascade save
            order.getPaymentTransactions().add(txn);
            // We'll save txn after order is saved to get order ID, or cascade if mapped
        }

        order.setOrderDate(LocalDateTime.now());
        order.setOrderStatus("PENDING");
        order.setTotalAmount(summary.getTotalAmount());

        // Payment fields
        order.setDepositRequired(summary.isDepositRequired());
        order.setDepositAmount(summary.getDepositAmount());
        order.setDepositPaid(false);
        order.setCouponCode(summary.getCouponCode());
        order.setDiscountAmount(summary.getDiscountAmount());

        // Bank account ref
        if ("BANKING".equals(paymentMethod) && bankAccountId != null) {
            BankAccount bankAccount = bankAccountRepository.findById(bankAccountId)
                    .orElse(null);
            order.setBankAccount(bankAccount);
        }

        // Save order
        order = orderRepository.save(order);

        // 5. Create OrderDetails & Update Stock
        List<OrderDetail> orderDetailsList = new ArrayList<>();
        for (CartItem item : cartItems) {
            Watch watch = item.getWatch();

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setWatch(watch);
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(watch.getPrice());

            // Calculate item-level discount (from product discount, NOT coupon)
            BigDecimal prodDiscount = BigDecimal.ZERO;
            if (watch.getDiscountPercent() != null && watch.getDiscountPercent() > 0) {
                prodDiscount = watch.getPrice()
                        .multiply(BigDecimal.valueOf(watch.getDiscountPercent()))
                        .divide(BigDecimal.valueOf(100));
            }
            detail.setDiscountAmount(prodDiscount);

            BigDecimal priceAfterDiscount = watch.getPrice().subtract(prodDiscount);
            detail.setSubtotal(priceAfterDiscount.multiply(new BigDecimal(item.getQuantity())));

            orderDetailRepository.save(detail);
            orderDetailsList.add(detail); // Add to list for email

            // Update stock
            watch.setStockQuantity(watch.getStockQuantity() - item.getQuantity());
            watch.setSoldCount(watch.getSoldCount() + item.getQuantity());
            watchRepository.save(watch);
        }

        // 6. Handle Coupon Usage
        if (summary.getCouponCode() != null) {
            couponService.markCouponUsed(summary.getCouponCode());
        }

        // 7. Send Order Confirmation Email
        try {
            mailService.sendOrderConfirmation(order, orderDetailsList);
        } catch (Exception e) {
            // Log error but don't fail order placement
            System.err.println("Failed to send order confirmation email: " + e.getMessage());
            e.printStackTrace();
        }

        // 8. Clear Cart
        cartService.clearCart();

        return order;
    }
}
