package boiz.shop._2BShop.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDto(
        Integer orderId,
        String orderCode,
        String receiverName,
        String shippingPhone,
        String shippingAddress,
        String orderStatus,
        String notes,
        BigDecimal totalAmount,
        BigDecimal discountAmount,
        Boolean depositRequired,
        BigDecimal depositAmount,
        Boolean depositPaid,
        BigDecimal remainingAmount,
        LocalDateTime orderDate,
        LocalDateTime updatedDate,
        PaymentMethodDto paymentMethod,
        BankAccountDto bankAccount,
        AuthUserDto user,
        List<OrderDetailItemDto> orderDetails) {
}
