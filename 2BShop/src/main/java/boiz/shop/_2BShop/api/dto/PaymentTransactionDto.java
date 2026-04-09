package boiz.shop._2BShop.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentTransactionDto(
        Integer transactionId,
        String transactionCode,
        BigDecimal amount,
        String status,
        LocalDateTime transactionDate,
        String responseData,
        PaymentMethodDto paymentMethod,
        Integer orderId,
        String orderCode,
        String customerName) {
}
